package org.medibloc.panacea.crypto;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class AES256CTRTest {
    @Test
    public void testDecryptData() throws Exception {
        String accessKey = "this is access key !";
        String data = "hello medibloc!";
        String encryptedData = "cc3ecbfc39c59fcab796d63458ff27fb:a32ae9c5c19068c6a3c90f57cc8662";
        assertEquals(data, AES256CTR.decryptData(accessKey, encryptedData));
    }

    @Test
    public void testEncryptAndDecryptData() throws Exception {
        String accessKey = "this is access key !";
        String data = "hello medibloc!";
        String encryptedData = AES256CTR.encryptData(accessKey, data);
        assertEquals(data, AES256CTR.decryptData(accessKey, encryptedData));
    }
}
