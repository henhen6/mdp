package top.mddata.open.admin.service.bo;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 多线程任务处理类
 * @author henhen
 */
@Slf4j
public class MultiThreadTaskProcessor {
    // 线程池默认配置（可根据业务调整，或通过构造函数注入）
    private static final int DEFAULT_QUEUE_CAPACITY = 100; // 有界队列容量，避免无界队列OOM
    private static final long KEEP_ALIVE_TIME = 60L; // 线程空闲存活时间
    private static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;
    private static final long AWAIT_TERMINATION_TIMEOUT = 1; // 等待任务完成超时时间
    private static final TimeUnit AWAIT_TERMINATION_TIME_UNIT = TimeUnit.HOURS;

    /**
     * 使用N个线程处理任务，每个线程处理M个List
     *
     * @param tasks           任务列表的列表
     * @param numberOfThreads 线程数量N（核心/最大线程数）
     */
    public void processTasks(List<List<Task>> tasks, int numberOfThreads) {
        // 校验入参，避免非法值
        if (tasks == null || tasks.isEmpty()) {
            log.warn("任务列表为空，无需处理");
            return;
        }
        if (numberOfThreads <= 0) {
            log.error("线程数量必须大于0，当前值：{}", numberOfThreads);
            throw new IllegalArgumentException("线程数量必须大于0");
        }

        // 1. 自定义线程工厂（命名线程，便于日志排查）
        ThreadFactory threadFactory = new CustomThreadFactory("task-processor-");

        // 2. 手动创建ThreadPoolExecutor（核心优化点）
        // 核心线程数=最大线程数=指定线程数（固定线程池模式）
        // 用有界ArrayBlockingQueue替代默认无界LinkedBlockingQueue，避免OOM
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                numberOfThreads, // 核心线程数
                numberOfThreads, // 最大线程数（固定线程池，核心=最大）
                KEEP_ALIVE_TIME, // 线程空闲时间（核心线程不回收，此参数无实际意义）
                TIME_UNIT, // 时间单位
                new ArrayBlockingQueue<>(DEFAULT_QUEUE_CAPACITY), // 有界工作队列
                threadFactory, // 自定义线程工厂
                new CustomRejectedExecutionHandler() // 自定义拒绝策略
        );

        try {
            // 计算每个线程分配的任务列表数（向上取整）
            int listsPerThread = (int) Math.ceil((double) tasks.size() / numberOfThreads);

            // 分配任务到线程池
            for (int i = 0; i < numberOfThreads && i * listsPerThread < tasks.size(); i++) {
                int startIndex = i * listsPerThread;
                int endIndex = Math.min(startIndex + listsPerThread, tasks.size());

                // 注意：subList是原列表的视图，转为新List避免原列表修改影响
                List<List<Task>> threadTasks = List.copyOf(tasks.subList(startIndex, endIndex));

                // 提交任务到线程池
                executor.submit(new TaskWorker(threadTasks, i));
                log.debug("线程池提交第{}个工作任务，处理列表范围：[{}, {})", i, startIndex, endIndex);
            }

            // 3. 优雅关闭线程池：先停止接收新任务，等待已提交任务完成
            executor.shutdown();
            log.info("线程池已关闭接收新任务，等待所有任务完成（超时时间：{} {}）",
                    AWAIT_TERMINATION_TIMEOUT, AWAIT_TERMINATION_TIME_UNIT);

            // 等待所有任务完成，超时则强制关闭
            if (!executor.awaitTermination(AWAIT_TERMINATION_TIMEOUT, AWAIT_TERMINATION_TIME_UNIT)) {
                log.warn("任务处理超时，强制关闭线程池");
                // 强制关闭，中断未完成的任务
                List<Runnable> unfinishedTasks = executor.shutdownNow();
                log.error("线程池强制关闭，未完成的任务数：{}", unfinishedTasks.size());
            } else {
                log.info("所有任务处理完成，线程池正常关闭");
            }

        } catch (InterruptedException e) {
            // 恢复线程中断状态，避免丢失中断信号
            Thread.currentThread().interrupt();
            log.error("任务处理过程被中断", e);
            // 强制关闭线程池，清理资源
            executor.shutdownNow();
        } catch (Exception e) {
            log.error("任务分配/执行过程中发生异常", e);
            executor.shutdownNow();
        }
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

    /**
     * 任务接口
     */
    public interface Task {
        void execute();
    }
}
