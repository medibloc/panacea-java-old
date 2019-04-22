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

    @Test
    public void testGetSharedSecretKey() throws Exception {
        for (int my = 0; my < SampleKeys.keyPairs.length - 1; my++) {
            int other = my + 1;

//            String secretKey1 = Keys.getSharedSecretKey(SampleKeys.keyPairs[my][0], SampleKeys.keyPairs[other][1]);
//            String secretKey2 = Keys.getSharedSecretKey(SampleKeys.keyPairs[other][0], SampleKeys.keyPairs[my][1]);
            String secretKey1 = Keys.getSharedSecretKey("9d10d24d7883c35f11dce98ba4da737f209808001748a595728dc326aa008b60", "03349913aad7662ff63e3d200680a1773085184ccf34eca9022e76eabb53d55c98");
            String secretKey2 = Keys.getSharedSecretKey("a86a52fe76e0299298a4daf595174f94cbb2c21cc67c7d90505e4fec20511fce", "027d31268680a3de375fb57d9fcf724fa95a7dfaa3a3381c910ccc24e1c0cb80ee");
            assertEquals(secretKey1, secretKey2);
            System.out.println(secretKey1);
        }
    }
}
