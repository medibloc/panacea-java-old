package org.med4j.data;

import org.med4j.crypto.Hash;

public class Data {
    // TODO: replace this class with PHR module

    public static byte[] hashRecord(String data) {
        return Hash.sha3256(data.getBytes());
    }
}
