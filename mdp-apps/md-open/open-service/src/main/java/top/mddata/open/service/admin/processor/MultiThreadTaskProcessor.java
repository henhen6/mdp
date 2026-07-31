package top.mddata.open.service.admin.processor;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 多线程任务处理类（优化版：支持异步任务执行，调用后立即返回）
 * @author henhen
 */
@Slf4j
@Component
public class MultiThreadTaskProcessor {
    // 线程池默认配置（可根据业务调整，或通过构造函数注入）
    private static final int DEFAULT_CORE_THREADS = 10;
    private static final int DEFAULT_MAX_THREADS = 20;
    private static final int DEFAULT_QUEUE_CAPACITY = 100; // 有界队列容量，避免无界队列OOM
    private static final long KEEP_ALIVE_TIME = 60L; // 线程空闲存活时间
    private static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;
    private static final long AWAIT_TERMINATION_TIMEOUT = 1; // 等待任务完成超时时间
    private static final TimeUnit AWAIT_TERMINATION_TIME_UNIT = TimeUnit.HOURS;

    // 成员变量：线程池（复用，支持异步任务提交）
    private final ThreadPoolExecutor executor;
    // 标记线程池是否已关闭，避免重复关闭
    private final AtomicBoolean isShutdown = new AtomicBoolean(false);

    // ==================== 构造函数（增强灵活性） ====================

    /**
     * 默认构造函数：使用默认线程池参数
     */
    public MultiThreadTaskProcessor() {
        this(DEFAULT_CORE_THREADS, DEFAULT_MAX_THREADS, DEFAULT_QUEUE_CAPACITY);
    }

    /**
     * 自定义构造函数：支持传入线程池核心参数
     * @param coreThreads 核心线程数
     * @param maxThreads 最大线程数
     * @param queueCapacity 队列容量
     */
    public MultiThreadTaskProcessor(int coreThreads, int maxThreads, int queueCapacity) {
        // 1. 自定义线程工厂（命名线程，便于日志排查）
        ThreadFactory threadFactory = new CustomThreadFactory("task-processor-");
        // 2. 自定义拒绝策略
        RejectedExecutionHandler rejectedHandler = new CustomRejectedExecutionHandler();
        // 3. 初始化线程池（成员变量，复用）
        this.executor = new ThreadPoolExecutor(
                coreThreads,
                maxThreads,
                KEEP_ALIVE_TIME,
                TIME_UNIT,
                new ArrayBlockingQueue<>(queueCapacity),
                threadFactory,
                rejectedHandler
        );
        log.info("异步线程池初始化完成，核心线程数：{}，最大线程数：{}，队列容量：{}",
                coreThreads, maxThreads, queueCapacity);
    }

    /**
     * 优化后的异步任务处理方法：提交任务后立即返回，不阻塞调用线程
     * @param tasks 任务列表的列表
     * @param numberOfThreads 处理线程数（用于任务分配，不影响线程池核心参数）
     * @return 任务Future列表（用于跟踪任务执行状态/结果，可选）
     */
    public List<Future<?>> processTasksAsync(List<List<Task>> tasks, int numberOfThreads) {
        // 校验入参，避免非法值
        if (tasks == null || tasks.isEmpty()) {
            log.warn("任务列表为空，无需处理");
            return new ArrayList<>();
        }
        if (numberOfThreads <= 0) {
            log.error("线程数量必须大于0，当前值：{}", numberOfThreads);
            throw new IllegalArgumentException("线程数量必须大于0");
        }
        if (isShutdown.get()) {
            log.error("线程池已关闭，无法提交新任务");
            throw new IllegalStateException("线程池已关闭，禁止提交新任务");
        }

        // 存储任务Future，便于调用方跟踪执行状态
        List<Future<?>> futureList = new ArrayList<>();

        try {
            // 计算每个线程分配的任务列表数（向上取整）
            int listsPerThread = (int) Math.ceil((double) tasks.size() / numberOfThreads);

            // 分配任务到线程池（异步提交，立即返回）
            for (int i = 0; i < numberOfThreads && i * listsPerThread < tasks.size(); i++) {
                int startIndex = i * listsPerThread;
                int endIndex = Math.min(startIndex + listsPerThread, tasks.size());

                // 注意：subList是原列表的视图，转为新List避免原列表修改影响
                List<List<Task>> threadTasks = List.copyOf(tasks.subList(startIndex, endIndex));

                // 异步提交任务到线程池，返回Future存入列表
                Future<?> future = executor.submit(new TaskWorker(threadTasks, i));
                futureList.add(future);

                log.debug("异步线程池提交第{}个工作任务，处理列表范围：[{}, {})", i, startIndex, endIndex);
            }

            log.info("所有任务已异步提交到线程池，共提交{}个工作任务，等待后台执行", futureList.size());
            return futureList;

        } catch (Exception e) {
            log.error("任务异步提交过程中发生异常", e);
            // 若提交失败，返回已提交的Future列表（便于调用方处理）
            return futureList;
        }
    }

    /**
     * 优雅关闭线程池（供调用方在合适时机调用，如应用关闭时）
     * 阻塞直到所有任务完成或超时
     */
    public void shutdownGracefully() {
        if (isShutdown.compareAndSet(false, true)) {
            log.info("开始优雅关闭异步线程池...");
            // 1. 停止接收新任务
            executor.shutdown();

            try {
                // 2. 等待所有已提交任务完成
                if (!executor.awaitTermination(AWAIT_TERMINATION_TIMEOUT, AWAIT_TERMINATION_TIME_UNIT)) {
                    log.warn("任务执行超时，强制关闭线程池");
                    // 3. 超时未完成，强制关闭，中断未完成的任务
                    List<Runnable> unfinishedTasks = executor.shutdownNow();
                    log.error("线程池强制关闭，未完成的任务数：{}", unfinishedTasks.size());
                } else {
                    log.info("所有异步任务处理完成，线程池正常关闭");
                }
            } catch (InterruptedException e) {
                // 恢复线程中断状态，避免丢失中断信号
                Thread.currentThread().interrupt();
                log.error("线程池关闭过程被中断", e);
                // 强制关闭线程池，清理资源
                executor.shutdownNow();
            }
        } else {
            log.warn("线程池已关闭，无需重复执行关闭操作");
        }
    }

    @PreDestroy
    public void destroy() {
        this.shutdownGracefully();
    }

    /**
     * 获取线程池当前状态
     */
    public boolean isThreadPoolShutdown() {
        return isShutdown.get();
    }

    /**
     * 获取当前活跃线程数
     */
    public int getActiveThreadCount() {
        return executor.getActiveCount();
    }

    /**
     * 任务接口
     */
    public interface Task {
        void execute();
    }

    /**
     * 工作线程类
     * 优化：捕获单个任务执行异常，避免一个任务失败导致整个线程的任务中断
     */
    private static class TaskWorker implements Runnable {
        private final List<List<Task>> taskLists;
        private final int workerId;

        TaskWorker(List<List<Task>> taskLists, int workerId) {
            this.taskLists = taskLists;
            this.workerId = workerId;
        }

        @Override
        public void run() {
            log.info("工作线程 {} 开始处理 {} 个任务列表", workerId, taskLists.size());

            // 处理分配给该线程的所有任务列表
            for (int listIndex = 0; listIndex < taskLists.size(); listIndex++) {
                List<Task> taskList = taskLists.get(listIndex);
                log.debug("工作线程 {} 处理第 {} 个任务列表，包含 {} 个任务",
                        workerId, listIndex, taskList.size());

                // 处理单个任务列表中的所有任务
                for (Task task : taskList) {
                    try {
                        // 执行单个任务，捕获异常避免影响后续任务
                        processTask(task);
                    } catch (Exception e) {
                        log.error("工作线程 {} 处理任务时发生异常，任务列表索引：{}", workerId, listIndex, e);
                    }
                }
            }

            log.info("工作线程 {} 完成所有任务列表处理", workerId);
        }

        /**
         * 处理单个任务
         */
        private void processTask(Task task) {
            if (task == null) {
                log.warn("工作线程 {} 跳过空任务", workerId);
                return;
            }
            task.execute();
        }
    }

    // ==================== 辅助方法（可选） ====================

    /**
     * 自定义线程工厂：统一命名线程，便于日志排查和问题定位
     */
    private static class CustomThreadFactory implements ThreadFactory {
        private final String prefix; // 线程名称前缀
        private final AtomicInteger threadNumber = new AtomicInteger(1); // 线程计数器

        CustomThreadFactory(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, prefix + threadNumber.getAndIncrement());
            thread.setDaemon(false); // 非守护线程，确保任务完成
            thread.setPriority(Thread.NORM_PRIORITY); // 正常优先级
            // 捕获线程未处理的异常，避免线程意外终止
            thread.setUncaughtExceptionHandler((t, e) ->
                    log.error("线程 {} 发生未捕获异常", t.getName(), e));
            return thread;
        }
    }

    /**
     * 自定义拒绝策略：打印日志+调用者线程执行（避免任务丢失，降级处理）
     */
    private static class CustomRejectedExecutionHandler implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            log.warn("线程池队列已满，任务被拒绝执行（核心线程数：{}，队列容量：{}，活跃线程数：{}）",
                    executor.getCorePoolSize(),
                    executor.getQueue().size(),
                    executor.getActiveCount());
            // 降级策略：由调用者线程执行任务，避免任务丢失
            if (!executor.isShutdown()) {
                try {
                    r.run();
                    log.info("拒绝的任务已由调用者线程执行完成");
                } catch (Exception e) {
                    log.error("调用者线程执行拒绝任务时发生异常", e);
                }
            }
        }
    }
}