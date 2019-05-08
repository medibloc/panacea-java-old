package org.medibloc.panacea.crypto;

import org.junit.Test;
import org.medibloc.panacea.utils.Numeric;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.medibloc.panacea.crypto.Keys.PBKDF2_KEY_SIZE;
import static org.medibloc.panacea.crypto.Keys.compressPubKey;

public class KeysTest {
    @Test
    public void testGenerateKeyPair() throws Exception {
        ECKeyPair ecKeyPair = Keys.generateKeyPair();
        System.out.println("private key - " + ecKeyPair.getPrivKey().toString(16));
        System.out.println("public  key - " + ecKeyPair.getPubKey().toString(16));
        System.out.println("blockchain address - " + Keys.compressPubKey(ecKeyPair.getPubKey()));
    }

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
    public void testGetSharedSecretKey() {
        assertEquals("8b9726caa6bce4b438adbe41fcc9822c34714e1185fdff8eee5483282dd58f42",
                Keys.getSharedSecretKey("9d10d24d7883c35f11dce98ba4da737f209808001748a595728dc326aa008b60"
                        , "02dc01a49f2867a44e7a0fd08fb4a3e5a3c628d35ac6c444b1acc48617b4158458"));

        assertEquals("dc0e94384e0ed26e374dcc32d6c248228472d5b7032b7ffcbe9e8e905d5514e7",
                Keys.getSharedSecretKey("9750c852613084d5b424fdba9cfa128ab56c2252a562736975a38cc8500bd7d2"
                        , "039ff8fabfb2cac5f5b27d3fdea52e0030f1a0e71c010dd66ed64141c026e7048f"));

        for (int my = 0; my < SampleKeys.keyPairs.length; my++) {
            int other = (my + 1) % SampleKeys.keyPairs.length;

            String secretKey1 = Keys.getSharedSecretKey(SampleKeys.keyPairs[my][0], SampleKeys.keyPairs[other][1]);
            String secretKey2 = Keys.getSharedSecretKey(SampleKeys.keyPairs[other][0], SampleKeys.keyPairs[my][1]);
            assertEquals(secretKey1, secretKey2);
            System.out.println(secretKey1);
        }
    }
}
