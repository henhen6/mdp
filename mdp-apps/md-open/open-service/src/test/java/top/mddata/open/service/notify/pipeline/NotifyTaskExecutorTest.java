package top.mddata.open.service.notify.pipeline;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import top.mddata.open.enumeration.admin.ExecStatusEnum;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotifyTaskExecutorTest {

    /** 内存任务：记录状态推进 */
    static class FakeTask implements NotifyTaskView {
        Long id = 1L;
        String execStatus = ExecStatusEnum.WAIT.getCode();
        int requestCnt = 0;
        LocalDateTime lastRequestTime;
        LocalDateTime nextRequestTime;

        @Override public Long getId() { return id; }
        @Override public String getNotifyUrl() { return "http://x"; }
        @Override public String getAppKey() { return "ak"; }
        @Override public Integer getNotifyEncryptionType() { return 0; }
        @Override public String getNotifyToken() { return "t"; }
        @Override public String getNotifyEncodingAesKey() { return "k"; }
        @Override public Integer getRequestCnt() { return requestCnt; }
        @Override public String getExecStatus() { return execStatus; }
        @Override public String buildPlaintext() { return "{}"; }
        @Override public void markDelivering(LocalDateTime now) { requestCnt++; lastRequestTime = now; }
        @Override public void markStatus(String status, LocalDateTime next) { execStatus = status; nextRequestTime = next; }
    }

    /** 内存执行器：loadTask 返回预置任务，persist/appendLog 记录调用 */
    static class FakeExecutor extends NotifyTaskExecutor<FakeTask> {
        FakeTask task;
        boolean persisted;
        NotifyResult loggedResult;
        String loggedStatus;

        FakeExecutor(NotifySender sender, FakeTask task) {
            super(sender, new RetryPolicy("2m,5m"));
            this.task = task;
        }

        @Override protected FakeTask loadTask(Long taskId) { return task; }
        @Override protected void persist(FakeTask t) { persisted = true; }
        @Override protected void appendLog(FakeTask t, NotifyResult result) {
            loggedResult = result;
            loggedStatus = t.getExecStatus();
        }
    }

    private NotifySender sender;

    @BeforeEach
    void setUp() {
        sender = mock(NotifySender.class);
    }

    @Test
    void 任务不存在直接跳过() {
        FakeExecutor executor = new FakeExecutor(sender, null);
        executor.execute(99L);
        verify(sender, never()).send(any());
    }

    @Test
    void 终态任务幂等跳过() {
        FakeTask task = new FakeTask();
        task.execStatus = ExecStatusEnum.RETRY_OVER.getCode();
        FakeExecutor executor = new FakeExecutor(sender, task);
        executor.execute(1L);
        verify(sender, never()).send(any());
    }

    @Test
    void 投递成功置SUCCESS并写日志() {
        when(sender.send(any())).thenReturn(NotifyResult.ok("u", "q", "r", LocalDateTime.now(), LocalDateTime.now()));
        FakeTask task = new FakeTask();
        FakeExecutor executor = new FakeExecutor(sender, task);
        executor.execute(1L);
        assertEquals(ExecStatusEnum.SUCCESS.getCode(), task.execStatus);
        assertEquals(1, task.requestCnt);
        assertTrue(executor.persisted);
        assertNotNull(executor.loggedResult);
    }

    @Test
    void 投递失败且次数未满置FAIL并算下次时间() {
        when(sender.send(any())).thenReturn(NotifyResult.fail("x", "u", "q", null, LocalDateTime.now(), LocalDateTime.now()));
        FakeTask task = new FakeTask();
        FakeExecutor executor = new FakeExecutor(sender, task);
        executor.execute(1L);
        assertEquals(ExecStatusEnum.FAIL.getCode(), task.execStatus);
        assertNotNull(task.nextRequestTime);
    }

    @Test
    void 投递失败且次数用尽置RETRY_OVER() {
        when(sender.send(any())).thenReturn(NotifyResult.fail("x", "u", "q", null, LocalDateTime.now(), LocalDateTime.now()));
        FakeTask task = new FakeTask();
        task.requestCnt = 1; // 梯度只有 2m,5m 两级，本次为第 2 次，失败即用尽
        FakeExecutor executor = new FakeExecutor(sender, task);
        executor.execute(1L);
        assertEquals(ExecStatusEnum.RETRY_OVER.getCode(), task.execStatus);
        assertNull(task.nextRequestTime);
    }
}
