package org.medibloc.panacea.tx;

import com.google.protobuf.ByteString;
import org.medibloc.panacea.account.Account;
import org.medibloc.panacea.account.AccountUtils;
import org.medibloc.panacea.core.protobuf.BlockChain;
import org.medibloc.panacea.core.protobuf.BlockChain.TransactionHashTarget;
import org.medibloc.panacea.core.protobuf.Rpc;
import org.medibloc.panacea.core.protobuf.Rpc.SendTransactionRequest;
import org.medibloc.panacea.crypto.Hash;
import org.medibloc.panacea.crypto.Sign;
import org.medibloc.panacea.utils.Numeric;

public class Transaction {
    private static final int VALUE_SIZE = 16;
    private static final int ADDRESS_SIZE = 16;

    private class Type {
        static final String ADD_RECORD = "add_record";
    }

    public static String hashTx(TransactionHashTarget hashTarget) {
        byte[] bytes = hashTarget.toByteArray();
        return Numeric.byteArrayToHex(Hash.sha3256(bytes));
    }

    public static BlockChain.TransactionHashTarget getAddRecordTransactionHashTarget(byte[] dataHash, String fromAddress, long nonce, int chainId) {
        BlockChain.AddRecordPayload payload = BlockChain.AddRecordPayload.newBuilder()
                .setHash(ByteString.copyFrom(dataHash))
                .build();

        return BlockChain.TransactionHashTarget.newBuilder()
                .setTxType(Type.ADD_RECORD)
                .setFrom(ByteString.copyFrom(Numeric.hexStringToByteArray(fromAddress)))
                .setTo(ByteString.copyFrom(new byte[ADDRESS_SIZE])) // default value
                .setValue(ByteString.copyFrom(new byte[VALUE_SIZE])) // default value
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
                .setSign(Numeric.toHexStringNoPrefix(sign.getR()) + Numeric.toHexStringNoPrefix(sign.getS()) + String.format("%02x", recoveryCode & 0xFF))
                .build();
    }

    private static SendTransactionRequest.Builder getTxRequestBuilder(BlockChain.TransactionHashTarget txHashTarget) {
        return SendTransactionRequest.newBuilder()
                .setChainId(txHashTarget.getChainId())
                .setNonce(txHashTarget.getNonce())
                .setPayload(Numeric.toHexStringNoPrefix(txHashTarget.getPayload().toByteArray()))
                .setTo(Numeric.toHexStringNoPrefix(txHashTarget.getTo().toByteArray()))
                .setTxType(txHashTarget.getTxType())
                .setValue(Numeric.toHexStringNoPrefix(txHashTarget.getValue().toByteArray()));
    }
}
