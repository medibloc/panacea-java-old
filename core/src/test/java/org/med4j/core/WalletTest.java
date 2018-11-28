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
        WalletCreator.create(null, null);
    }
}
