package top.mddata.sdk.test.rsa;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.mddata.sdk.core.sign.RsaTool;
import top.mddata.sdk.test.BaseTest;

/**
 * RSA加解密工具。
 *
 * @author henhen6
 */

public class RsaToolTest extends BaseTest {
    private static final Logger LOG = LoggerFactory.getLogger(RsaToolTest.class);

    @Test
    public void testGeneratePublicKeyPrivateKey1() throws Exception {
        RsaTool.KeyFormat keyFormat = RsaTool.KeyFormat.PKCS1;
        RsaTool rsaTool = new RsaTool(keyFormat, RsaTool.KeyLength.LENGTH_2048);
        RsaTool.KeyStore keys = rsaTool.createKeys();

        LOG.info("公钥: " + keys.getPublicKey());
        LOG.info("--------------------------");
        LOG.info("私钥: " + keys.getPrivateKey());
    }

    @Test
    public void testGeneratePublicKeyPrivateKey2() throws Exception {
        RsaTool.KeyFormat keyFormat = RsaTool.KeyFormat.PKCS1;
        RsaTool rsaTool = new RsaTool(keyFormat, RsaTool.KeyLength.LENGTH_1024);
        RsaTool.KeyStore keys = rsaTool.createKeys();

        LOG.info("公钥: " + keys.getPublicKey());
        LOG.info("--------------------------");
        LOG.info("私钥: " + keys.getPrivateKey());
    }

    @Test
    public void testGeneratePublicKeyPrivateKey3() throws Exception {
        RsaTool.KeyFormat keyFormat = RsaTool.KeyFormat.PKCS8;
        RsaTool rsaTool = new RsaTool(keyFormat, RsaTool.KeyLength.LENGTH_2048);
        RsaTool.KeyStore keys = rsaTool.createKeys();

        LOG.info("公钥: " + keys.getPublicKey());
        LOG.info("--------------------------");
        LOG.info("私钥: " + keys.getPrivateKey());
    }


    @Test
    public void testGeneratePublicKeyPrivateKey4() throws Exception {
        RsaTool.KeyFormat keyFormat = RsaTool.KeyFormat.PKCS8;
        RsaTool rsaTool = new RsaTool(keyFormat, RsaTool.KeyLength.LENGTH_1024);
        RsaTool.KeyStore keys = rsaTool.createKeys();

        LOG.info("公钥: " + keys.getPublicKey());
        LOG.info("--------------------------");
        LOG.info("私钥: " + keys.getPrivateKey());
    }

}
