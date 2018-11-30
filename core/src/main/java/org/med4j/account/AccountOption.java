package org.med4j.account;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.bouncycastle.crypto.generators.SCrypt;
import org.med4j.crypto.Keys;
import org.med4j.utils.Numeric;

import javax.crypto.Cipher;
import java.util.Arrays;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.med4j.crypto.SecureRandomUtils.secureRandom;

public class AccountOption {
    public AccountOption(boolean useDefaultSettings) {
        if (useDefaultSettings) {
            this.version = CURRENT_VERSION;

            Crypto crypto = new Crypto();
            crypto.cipher = AES_128_CTR;
            crypto.kdf = SCRYPT;

            ScryptKdfParams kdfParams = new ScryptKdfParams();
            kdfParams.setDklen(DKLEN);
            kdfParams.setN(N);
            kdfParams.setP(P);
            kdfParams.setR(R);
            crypto.setKdfparams(kdfParams);

            this.crypto = crypto;
        }
    }



}
