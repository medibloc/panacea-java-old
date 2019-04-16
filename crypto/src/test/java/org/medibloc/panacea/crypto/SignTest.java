package org.medibloc.panacea.crypto;

import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.*;

public class SignTest {
    private static final BigInteger PRIVATE_KEY = new BigInteger("6957772055e3f3587db5cbb5802dc67d8aa4bef5335ab4ee61ff7f5601fc89a7", 16);
    // address: 03107c5eae25e0443be09496162362fee885402379ee4c0fca30af8dbaa340e507
    private static final BigInteger PUBLIC_KEY = new BigInteger("107c5eae25e0443be09496162362fee885402379ee4c0fca30af8dbaa340e507933890e0c8f931351a9a37d7a151d1e8d9620b55adbe7a5e8663a4cea843f887", 16);
    private static final String BLOCKCHAIN_ADDRESS = "03107c5eae25e0443be09496162362fee885402379ee4c0fca30af8dbaa340e507";

    @Test
    public void testSignMessage() {
        String sampleNonce = "ff3ba7cbc598be308d864026b6a440d0a0c982737889695b36b696eeafd47eee";
        String expected = "77ca0fb1f8907030be5fe7386020e66a30e98220d9344cc3acdf6cfdd7b579db239732ec31e08ccb635a40561c28ae3dc440ad7c5904886776463edda538668001";

        ECKeyPair ecKeyPair = new ECKeyPair(PRIVATE_KEY, PUBLIC_KEY);
        String actual = Sign.signMessage(sampleNonce, ecKeyPair);
        assertEquals(expected, actual);
    }

    @Test
    public void testVerifyMessage() {
        for (String[] keySet: SampleKeys.mnemonicKeys) {
            String message = "ff3ba7cbc598be308d864026b6a440d0a0c982737889695b36b696eeafd47eee";

            ECKeyPair ecKeyPair = new ECKeyPair(new BigInteger(keySet[1], 16), new BigInteger(keySet[2], 16));
            String signature = Sign.signMessage(message, ecKeyPair);
            boolean actual = Sign.verifyMessage(keySet[3], message, signature);
            assertTrue(actual);

            String invalidMessage = "invalidMessage";
            actual = Sign.verifyMessage(BLOCKCHAIN_ADDRESS, invalidMessage, signature);
            assertFalse(actual);

            String wrongAddress = "03F07c5eae25e0443be09496162362fee885402379ee4c0fca30af8dbaa340e507";
            actual = Sign.verifyMessage(wrongAddress, message, signature);
            assertFalse(actual);
        }
    }
}
