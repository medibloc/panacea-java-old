package org.med4j.tx;

import com.google.protobuf.ByteString;
import org.med4j.account.Account;
import org.med4j.core.protobuf.BlockChain;
import org.med4j.core.protobuf.BlockChain.TransactionHashTarget;
import org.med4j.core.protobuf.Rpc;
import org.med4j.crypto.Hash;
import org.med4j.healthdata.HealthData;
import org.med4j.utils.Numeric;

import java.util.Calendar;

public class Transaction {
    static final int VALUE_SIZE = 16;

    private class Type {
        static final String ADD_RECORD = "add_record";
    }

    private class Algorithm {
        static final int SECP256K1 = 1;
        static final int SHA3256 = 2;
    }

    public static String hashTx(TransactionHashTarget hashTarget) {
        byte[] bytes = hashTarget.toByteArray();
        return Numeric.byteArrayToHex(Hash.sha3256(bytes));
    }

    public static void uploadDataHash(byte[] data, Account account, String password, long nonce, int chainId) {
        byte[] dataHash = HealthData.hashData(data);

        BlockChain.AddRecordPayload payload = BlockChain.AddRecordPayload.newBuilder()
                .setHash(ByteString.copyFrom(dataHash))
                .build();

        BlockChain.TransactionHashTarget txHashTarget = BlockChain.TransactionHashTarget.newBuilder()
                .setTxType(Type.ADD_RECORD)
                .setFrom(ByteString.copyFrom(Numeric.hexStringToByteArray(account.getAddress())))
                .setTo(ByteString.copyFrom(new byte[33])) // default value
                .setValue(ByteString.copyFrom(new byte[16])) // default value
                .setTimestamp(getCurrentTimeInSecond())
                .setNonce(nonce) // TODO : thread safe
                .setChainId(chainId) // TODO : read from config
                .setPayload(ByteString.copyFrom(payload.toByteArray()))
                .build();

        String sign = ""; // TODO

        Rpc.SendTransactionRequest request = getTxRequestBuilderWithValuesOf(txHashTarget, hashTx(txHashTarget))
                .setHashAlg(Algorithm.SHA3256)
                .setCryptoAlg(Algorithm.SECP256K1)
                //.setPayerSign(null)
                .setSign(sign)
                .build();

        // TODO - send request
    }

    private static long getCurrentTimeInSecond() {
        return Calendar.getInstance().getTimeInMillis() / 1000;
    }

    private static Rpc.SendTransactionRequest.Builder getTxRequestBuilderWithValuesOf(BlockChain.TransactionHashTarget txHashTarget, String hash) {
        return Rpc.SendTransactionRequest.newBuilder()
                .setHash(hash)
                .setChainId(txHashTarget.getChainId())
                .setNonce(txHashTarget.getNonce())
                .setTimestamp(txHashTarget.getTimestamp())
                .setPayloadBytes(txHashTarget.getPayload())
                .setTo(txHashTarget.getTo().toString())
                .setTxType(txHashTarget.getTxType())
                .setValueBytes(txHashTarget.getValue());
    }
}
