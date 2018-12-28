package org.med4j.crypto;

import org.junit.Test;
import org.med4j.utils.Numeric;

import java.math.BigInteger;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
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
            assertEquals(mnemonicKey[2], Keys.compressPubKey(pubKey));
        }
    }

    @Test
    public void testPubKeyFromPrivKey() throws Exception {
        for (String[] keyPair: SampleKeys.keyPairs) {
            BigInteger actual = Keys.getPublicKeyFromPrivatekey(new BigInteger(keyPair[0], 16));
            System.out.println("\nprivate key : " + keyPair[0]);
            System.out.println("uncompressed public key : " + actual.toString(16));
            assertEquals(keyPair[1], Keys.compressPubKey(actual));
        }
    }
}
