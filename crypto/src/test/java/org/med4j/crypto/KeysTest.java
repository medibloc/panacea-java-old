package org.med4j.crypto;

import org.junit.Test;
import org.med4j.utils.Numeric;

import java.math.BigInteger;

import static org.junit.Assert.*;
import static org.med4j.crypto.Keys.PBKDF2_KEY_SIZE;

public class KeysTest {
    @Test
    public void testKeyPairFromPassphrase() throws Exception {
        for (String[] mnemonicKey: SampleKeys.mnemonicKeys) {
            ECKeyPair ecKeyPair = Keys.generateKeysFromPassphrase(mnemonicKey[0]);

            BigInteger privKey = ecKeyPair.getPrivKey();
            BigInteger pubKey = ecKeyPair.getPubKey();
            assertNotNull(privKey);
            assertNotNull(pubKey);

            assertEquals(mnemonicKey[1], Numeric.toHexStringZeroPadded(privKey, PBKDF2_KEY_SIZE * 2));
        }
    }

    @Test
    public void testGetAddress() {
        String validAddress = "021afe7c3a1d74e8f76cf777fe1ecb89f8aeaccbe17ea1d5748e73e9f785e4e90b";
        ECKeyPair ecKeyPair = new ECKeyPair(null, new BigInteger(validAddress, 16));
        assertTrue(Keys.getAddress(ecKeyPair).equals(validAddress));
    }

    /*
    @Test
    public void testPubKeyFromPrivKey() throws Exception {
        String[] keyPair = SampleKeys.keyPairs[0];
        byte[] privKeyBytes = Numeric.hexStringToByteArray(keyPair[0]);
        byte[] pubKeyBytes = Numeric.hexStringToByteArray(keyPair[1]);
    }
    */
}
