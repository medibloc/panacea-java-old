package org.med4j.core;

import static org.junit.Assert.*;
import org.junit.Test;
import org.med4j.crypto.ECKeyPair;
import org.med4j.wallet.Wallet;
import org.med4j.wallet.WalletCreator;

import java.math.BigInteger;

public class WalletTest {
    @Test(expected = Exception.class)
    public void testCreate() throws Exception {
        WalletCreator.create(null, null, null);
    }

    @Test
    public void testGetAddress() {
        String validAddress = "021afe7c3a1d74e8f76cf777fe1ecb89f8aeaccbe17ea1d5748e73e9f785e4e90b";

        Wallet wallet = new Wallet();
        ECKeyPair ecKeyPair = new ECKeyPair(null, new BigInteger(validAddress, 16));
        wallet.setEcKeyPair(ecKeyPair);

        assertTrue(wallet.getAddress().equals(validAddress));
    }
}
