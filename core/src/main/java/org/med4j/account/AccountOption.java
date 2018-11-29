package org.med4j.account;

import java.util.UUID;

public class AccountOption {
    private static final int CURRENT_VERSION = 3;

    public int version;
    public String id;
    public Crypto crypto;

    public AccountOption() {
        AccountOption accountOption = new AccountOption();
        accountOption.version = CURRENT_VERSION;
        accountOption.id = UUID.randomUUID().toString();

        Crypto crypto = new Crypto();
        accountOption.crypto = crypto;
    }
}
