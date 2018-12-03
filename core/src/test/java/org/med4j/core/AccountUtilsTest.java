package org.med4j.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.med4j.account.Account;
import org.med4j.account.AccountUtils;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class AccountUtilsTest {
    @Test
    public void testAccountEquals() {
        try {
            Account account1 = AccountUtils.createAccount("account1", null);
            Account account2 = AccountUtils.createAccount("account2", null);

            assertNotEquals(account1, account2);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Test
    public void testCreateAccount() {
        try {
            Account account = AccountUtils.createAccount("abcd", null);

            File accountFile = new File("testCreateAccount.testresult");
            new ObjectMapper().writeValue(accountFile, account);
            System.out.println("Created Account file by AccountUtilsTest.testCreateAccount() : " + accountFile.getAbsolutePath());

            Account parsedAccount = new ObjectMapper().readValue(accountFile, Account.class);

            assertEquals(account, parsedAccount);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
