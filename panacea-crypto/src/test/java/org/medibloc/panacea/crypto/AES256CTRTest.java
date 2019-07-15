package org.medibloc.panacea.crypto;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class AES256CTRTest {
    @Test
    public void testDecryptData() throws Exception {
        String accessKey = "this is access key !";
        String data = "Hello 메디블록!";
        String encryptedData = "def57dee8ad895d44924506e264dde6a:c589a3451b71e769777dd03e5121400b3d4525";
        byte[] decryptedData = AES256CTR.decryptData(accessKey, encryptedData);
        assertEquals(data, new String(decryptedData));
    }

    @Test
    public void testEncryptAndDecryptData() throws Exception {
        String accessKey = "this is access key !";
        String data = "Hello 메디블록!";
        String encryptedData = AES256CTR.encryptData(accessKey, data);
        System.out.println("encryptedData: " + encryptedData);
        byte[] decryptedData = AES256CTR.decryptData(accessKey, encryptedData);
        assertEquals(data, new String(decryptedData));
    }
}
