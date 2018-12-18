package org.med4j.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.med4j.account.Account;
import org.med4j.account.AccountUtils;
import org.med4j.crypto.ECKeyPair;

import java.io.File;
import java.math.BigInteger;
import java.security.SecureRandom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class AccountUtilsTest {
    final static String SAMPLE_ACCOUNT_FILE_PATH = "sampleAccount.json";
    final static String SAMPLE_PASSWORD = "sample";

    @Test
    public void testAccountEquals() {
        try {
            Account account1 = AccountUtils.createAccount(SAMPLE_PASSWORD, null);
            Account account2 = AccountUtils.createAccount(SAMPLE_PASSWORD, null);

            assertNotEquals(account1, account2);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Test
    public void testCreateAccount() {
        try {
            Account account = AccountUtils.createAccount(generateRandomBytes(32), null);
            File accountFile = new File("testCreateAccount.testresult");
            new ObjectMapper().writeValue(accountFile, account);
            System.out.println("Created Account file by AccountUtilsTest.testCreateAccount() : " + accountFile.getAbsolutePath());

            Account parsedAccount = new ObjectMapper().readValue(accountFile, Account.class);

            assertEquals(account, parsedAccount);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Test
    public void testConvertAccountToJson() {
        try {
            Account createdAccount = AccountUtils.createAccount(generateRandomBytes(32), null);
            String jsonAccount = AccountUtils.convertAccountToJson(createdAccount);

            System.out.println(jsonAccount);

            Account parsedAccount = AccountUtils.parseJsonAccount(jsonAccount);
            assertEquals(createdAccount, parsedAccount);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Test
    public void testSaveAccountToDefaultPath() {
        try {
            Account createdAccount = AccountUtils.createAccount(generateRandomBytes(32), null);
            File savedFile = AccountUtils.saveAccountToDefaultPath(createdAccount);

            System.out.println("savedFile : " + savedFile.getAbsolutePath());
            Account loadedAccount = AccountUtils.loadAccount(savedFile.getPath());

            assertEquals(createdAccount, loadedAccount);

            //cleanup
            savedFile.delete();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Test
    public void testLoadAccount() throws Exception {
        ECKeyPair ecKeyPair = new ECKeyPair(
                new BigInteger("4627e66cd55fe54500bb0397663254564249b276f3bf81c21a3a06bd72dfcf74", 16)
                , new BigInteger("cb2bde8309a4bfde8e53be4e96a99082920fdccea0b5fddaf9d782d25a0e454f6cd4bbd19345cc3f3de58a8c11bb45e764bbacad507873e28e33c7f724bca1eb", 16));

        Account expected = AccountUtils.createAccount(SAMPLE_PASSWORD, ecKeyPair, null);
        Account actual = AccountUtils.loadAccount(SAMPLE_ACCOUNT_FILE_PATH);

        assertEquals(expected.getAddress(), actual.getAddress());
    }

    @Test
    public void testGetKeyPair() throws Exception {
        ECKeyPair expected = new ECKeyPair(
                new BigInteger("4627e66cd55fe54500bb0397663254564249b276f3bf81c21a3a06bd72dfcf74", 16)
                , new BigInteger("cb2bde8309a4bfde8e53be4e96a99082920fdccea0b5fddaf9d782d25a0e454f6cd4bbd19345cc3f3de58a8c11bb45e764bbacad507873e28e33c7f724bca1eb", 16));

        Account account = AccountUtils.createAccount(SAMPLE_PASSWORD, expected, null);

        ECKeyPair actual = AccountUtils.getKeyPair(account, SAMPLE_PASSWORD);

        assertEquals(expected, actual);
    }

    private static String generateRandomBytes(int size) {
        byte[] bytes = new byte[size];
        new SecureRandom().nextBytes(bytes);
        return new String(bytes);
    }
}
