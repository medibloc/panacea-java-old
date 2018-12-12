package org.med4j.tx;

import com.google.protobuf.ByteString;
import org.med4j.account.Account;
import org.med4j.core.protobuf.BlockChain.TransactionHashTarget;
import org.med4j.core.protobuf.Rpc;
import org.med4j.crypto.Hash;
import org.med4j.utils.Numeric;

public class Transaction {
    static final int VALUE_SIZE = 16;

    public static String hashTx(TransactionHashTarget hashTarget) {
        byte[] bytes = hashTarget.toByteArray();
        return Numeric.byteArrayToHex(Hash.sha3256(bytes));
    }

    public static Rpc.SendTransactionRequest getSendTransactionRequest(Account account, byte[] data) {
        TransactionHashTarget txHashTarget = TransactionHashTarget.newBuilder()
                .setNonce(1)
                .setPayload(ByteString.copyFrom(data))
                .setFrom(ByteString.copyFromUtf8(account.getAddress()))
                .setChainId(181112)
                .setTimestamp(1544610216)
                //.setTo(null)
                .setValue(ByteString.copyFromUtf8("0"))
                .setTxType("add_record")
                .build();

        // TODO - should be "e48c476c2fc1a21eee2a25a070bdb1f69788d6c0c3c84f7cfb84abae6868d9e3"
        String hash = hashTx(txHashTarget);
        // TODO - should be "2bca8e8dc3faef143018284f3e9aba02af375c9f0693c44fab8ff14dd41c3aee029167bb360413175cc40fe80a40a35e9b10f96aedcea0b6a85289a9dc53083300"
        String sign = "";

        Rpc.SendTransactionRequest request = Rpc.SendTransactionRequest.newBuilder()
                .setHashAlg(2)
                .setHash(hash)
                .setChainId(181112)
                .setCryptoAlg(1)
                .setNonce(1)
                .setTimestamp(1544610216)
                //.setPayerSign(null)
                .setPayload("abc")
                .setSign(sign)
                .setTo(account.getAddress())
                .setTxType("add_record")
                .setValue("0")
                .build();

        return request;
    }
}
