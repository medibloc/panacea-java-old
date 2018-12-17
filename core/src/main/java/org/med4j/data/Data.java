package org.med4j.data;

import org.med4j.crypto.Hash;

public class Data {
    public static byte[] hashRecord(String data) {
        // TODO : check type, validate format, merklize data, ...
        return Hash.sha3256(data.getBytes());
    }
}
