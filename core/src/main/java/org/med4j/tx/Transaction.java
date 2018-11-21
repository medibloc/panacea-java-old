package org.med4j.tx;

import org.med4j.core.protobuf.BlockChain;
import org.med4j.crypto.Hash;
import org.med4j.utils.Numeric;

public class Transaction {
    static final int VALUE_SIZE = 16;

    public static String hashTx(BlockChain.TransactionHashTarget hashTarget) {
        byte[] bytes = hashTarget.toByteArray();
        return Numeric.byteArrayToHex(Hash.sha3256(bytes));
    }
}
