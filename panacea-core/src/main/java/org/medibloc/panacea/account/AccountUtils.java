package org.medibloc.panacea.account;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.medibloc.panacea.crypto.ECKeyPair;
import org.medibloc.panacea.crypto.Keys;
import org.medibloc.panacea.key.KeyStore;

import java.io.File;

public class AccountUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /** Create new Account. A new key pair will be generated internally. */
    public static Account createAccount(String password, AccountOption accountOption) throws Exception {
        ECKeyPair ecKeyPair = Keys.generateKeyPair();
        return createAccount(password, ecKeyPair, accountOption);
    }

    /** Create new Account with the given key pair. */
    public static Account createAccount(String password, ECKeyPair ecKeyPair, AccountOption accountOption) throws Exception {
        if (accountOption == null) accountOption = new AccountOption();

        Account account = new Account(password, ecKeyPair, accountOption);

        return account;
    }

    public static String convertAccountToJson(Account account) throws Exception {
        return KeyStore.convertToJson(account);
    }

    public static Account parseJsonAccount(String jsonAccount) throws Exception {
        return KeyStore.parseJson(jsonAccount, Account.class);
    }

    public static File saveAccountToDefaultPath(Account account) throws Exception {
        return KeyStore.saveToDefaultPath(account, account.getAddress());
    }

    public static File saveAccount(Account account, String destinationPath) throws Exception {
        return KeyStore.save(account, account.getAddress(), destinationPath);
    }

    public static Account loadAccount(String accountFilePath) throws Exception {
        return objectMapper.readValue(new File(accountFilePath), Account.class);
    }

    public static ECKeyPair getKeyPair(Account account, String password) throws Exception {
        return account.getKeyPair(password);
    }
}
