package org.med4j.core;

import static org.junit.Assert.*;
import org.junit.Test;
import org.med4j.wallet.WalletCreator;

public class WalletTest {
    @Test(expected = IllegalArgumentException.class)
    public void TestCreate() {
        WalletCreator.create(null);
    }
}
