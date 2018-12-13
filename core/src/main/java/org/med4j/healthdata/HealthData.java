package org.med4j.healthdata;

import org.med4j.crypto.Hash;

public class HealthData {
    public static byte[] hashData(byte[] data) {
        return hashData(data, "", "");
    }

    public static byte[] hashData(byte[] data, String type, String subType) {
        // TODO : check type, validate format, merklize data, and so on...
        return Hash.sha3256(data);
    }
}
