package org.medibloc.panacea.crypto;

import org.junit.Test;
import org.medibloc.panacea.utils.Numeric;

import java.math.BigInteger;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.medibloc.panacea.crypto.Keys.PBKDF2_KEY_SIZE;
import static org.medibloc.panacea.crypto.Keys.compressPubKey;

public class KeysTest {

    @Test
    public void testKeyPairFromPassphrase() throws Exception {
        for (String[] mnemonicKey: SampleKeys.mnemonicKeys) {
            ECKeyPair ecKeyPair = Keys.generateKeyPair(mnemonicKey[0]);

            BigInteger privKey = ecKeyPair.getPrivKey();
            BigInteger pubKey = ecKeyPair.getPubKey();
            assertNotNull(privKey);
            assertNotNull(pubKey);

            assertEquals(mnemonicKey[1], Numeric.toHexStringZeroPadded(privKey, PBKDF2_KEY_SIZE * 2));
            assertEquals(mnemonicKey[3], Keys.compressPubKey(pubKey));
        }
    }

    @Test
    public void testPubKeyFromPrivKey() {
        for (String[] keyPair: SampleKeys.keyPairs) {
            BigInteger actual = Keys.getPublicKeyFromPrivatekey(new BigInteger(keyPair[0], 16));
            System.out.println("\nprivate key : " + keyPair[0]);
            System.out.println("uncompressed public key : " + actual.toString(16));
            assertEquals(keyPair[1], Keys.compressPubKey(actual));
        }
    }

    @Test
    public void testGetPublicKeyFromPrivatekey() {
        for (String[] mnemonicKey: SampleKeys.mnemonicKeys) {
            String strPrivKey = mnemonicKey[1];
            BigInteger publicKey = Keys.getPublicKeyFromPrivatekey(new BigInteger(strPrivKey, 16));
            System.out.println(publicKey.toString(16));
            assertEquals(mnemonicKey[3], compressPubKey(publicKey));
        }
    }
}
