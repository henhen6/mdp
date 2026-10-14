package top.mddata.sdk.core.aes;

import com.alibaba.fastjson2.JSONObject;
import org.junit.Test;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * 共享实例并发加密：每个线程的签名必须与自己线程的报文对应（旧版实例字段会串值）。
 */
public class MdpBizMsgCryptConcurrentTest {

    @Test
    public void testConcurrentEncrypt() throws Exception {
        MdpBizMsgCrypt crypt = new MdpBizMsgCrypt(
                MdpBizMsgCrypt.generateToken(),
                MdpBizMsgCrypt.generateEncodingAesKey(),
                "testAppKey");
        int threads = 16;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);
        Set<String> signatures = ConcurrentHashMap.newKeySet();
        Set<String> errors = ConcurrentHashMap.newKeySet();

        for (int i = 0; i < threads; i++) {
            final int index = i;
            pool.submit(() -> {
                try {
                    for (int j = 0; j < 50; j++) {
                        String plaintext = "{\"seq\":\"" + index + "-" + j + "\"}";
                        String timestamp = MdpBizMsgCrypt.generateTimestamp();
                        String nonce = MdpBizMsgCrypt.generateNonce();
                        EncryptResult result = crypt.encryptMsg(plaintext, timestamp, nonce,
                                MdpBizMsgCrypt.MODE_ENCRYPTED);
                        signatures.add(result.getSignature());
                        JSONObject body = result.getBody();
                        if (body == null || !body.containsKey("encrypt")) {
                            errors.add("报文缺 encrypt: " + index);
                        }
                    }
                } catch (Exception e) {
                    errors.add(e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        assertTrue(latch.await(30, TimeUnit.SECONDS));
        pool.shutdown();
        assertTrue("并发执行有错误: " + errors, errors.isEmpty());
        assertEquals("每次加密的签名应唯一", threads * 50, signatures.size());
    }
}
