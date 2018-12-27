package org.med4j.tx;

import com.google.protobuf.ByteString;
import org.med4j.account.Account;
import org.med4j.account.AccountUtils;
import org.med4j.core.protobuf.BlockChain;
import org.med4j.core.protobuf.BlockChain.TransactionHashTarget;
import org.med4j.core.protobuf.Rpc;
import org.med4j.core.protobuf.Rpc.SendTransactionRequest;
import org.med4j.crypto.Hash;
import org.med4j.crypto.Sign;
import org.med4j.utils.Numeric;

import java.util.Calendar;

public class Transaction {
    private static final int VALUE_SIZE = 16;
    private static final int ADDRESS_SIZE = 16;
    private static final long TIMESTAMP_NULL = 0;

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

    public static BlockChain.TransactionHashTarget getAddRecordTransactionHashTarget(byte[] dataHash, String fromAddress, long nonce, int chainId) {
        return getAddRecordTransactionHashTarget(dataHash, fromAddress, nonce, chainId, TIMESTAMP_NULL);
    }

    public static BlockChain.TransactionHashTarget getAddRecordTransactionHashTarget(byte[] dataHash, String fromAddress, long nonce, int chainId, long timeStamp) {
        timeStamp = timeStamp != TIMESTAMP_NULL ? timeStamp : getCurrentTimeInSecond();

        BlockChain.AddRecordPayload payload = BlockChain.AddRecordPayload.newBuilder()
                .setHash(ByteString.copyFrom(dataHash))
                .build();

        return BlockChain.TransactionHashTarget.newBuilder()
                .setTxType(Type.ADD_RECORD)
                .setFrom(ByteString.copyFrom(Numeric.hexStringToByteArray(fromAddress)))
                .setTo(ByteString.copyFrom(new byte[ADDRESS_SIZE])) // default value
                .setValue(ByteString.copyFrom(new byte[VALUE_SIZE])) // default value
                .setTimestamp(timeStamp)
                .setNonce(nonce)
                .setChainId(chainId)
                .setPayload(ByteString.copyFrom(payload.toByteArray()))
                .build();
    }

    public static Rpc.SendTransactionRequest getSignedTransactionRequest(BlockChain.TransactionHashTarget transactionHashTarget, Account account
            , String password) throws Exception {
        String hash = hashTx(transactionHashTarget);
        Sign.SignatureData sign = Sign.signMessage(Numeric.hexStringToByteArray(hash), AccountUtils.getKeyPair(account, password));
        int recoveryCode = (sign.getV() & 0xFF) - 27;

        return getTxRequestBuilder(transactionHashTarget)
                .setHash(hash)
                .setHashAlg(Algorithm.SHA3256)
                .setCryptoAlg(Algorithm.SECP256K1)
                .setSign(Numeric.toHexStringNoPrefix(sign.getR()) + Numeric.toHexStringNoPrefix(sign.getS()) + String.format("%02x", recoveryCode & 0xFF))
                .build();
    }

    private static long getCurrentTimeInSecond() {
        return Calendar.getInstance().getTimeInMillis() / 1000;
    }

    private static SendTransactionRequest.Builder getTxRequestBuilder(BlockChain.TransactionHashTarget txHashTarget) {
        return SendTransactionRequest.newBuilder()
                .setChainId(txHashTarget.getChainId())
                .setNonce(txHashTarget.getNonce())
                .setTimestamp(txHashTarget.getTimestamp())
                .setPayload(Numeric.toHexStringNoPrefix(txHashTarget.getPayload().toByteArray()))
                .setTo(Numeric.toHexStringNoPrefix(txHashTarget.getTo().toByteArray()))
                .setTxType(txHashTarget.getTxType())
                .setValue(Numeric.toHexStringNoPrefix(txHashTarget.getValue().toByteArray()));
    }
}
