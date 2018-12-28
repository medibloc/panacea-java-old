package org.medibloc.panacea.data;

import org.medibloc.panacea.crypto.Hash;

public class Data {
    // TODO: replace this class with PHR module

    public static byte[] hashRecord(String data) {
        return Hash.sha3256(data.getBytes());
    }
}
