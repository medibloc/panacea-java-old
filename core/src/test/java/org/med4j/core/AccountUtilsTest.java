package org.med4j.core;

import org.junit.Test;
import org.med4j.account.Account;
import org.med4j.account.AccountUtils;

public class AccountUtilsTest {
    @Test
    public void testCreateAccount() {
        Account account;
        try {
            account = AccountUtils.createAccount("abcd", null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // TODO : assert account.toJson() result
    }
}
