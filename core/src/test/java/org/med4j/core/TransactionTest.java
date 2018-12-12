package org.med4j.core;

import com.google.protobuf.ByteString;

import static org.junit.Assert.*;
import org.junit.Test;
import org.med4j.account.Account;
import org.med4j.account.AccountUtils;
import org.med4j.core.protobuf.BlockChain;
import org.med4j.core.protobuf.Rpc;
import org.med4j.tx.Transaction;
import org.med4j.utils.Numeric;

import java.io.File;
import java.math.BigInteger;

public class TransactionTest {

    private ByteString hexToByteString(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i+1), 16));
        }
        return ByteString.copyFrom(data);
    }

    private ByteString getAddress(String address) {
        int targetLength = 33 * 2;
        if (address.length() < targetLength) {
            int zeros = targetLength - address.length();
            address = String.format("%0" + zeros + "d", 0) + address;
        }
        return hexToByteString(address);
    }

    private ByteString getValue(String value) {
        // TODO VALUE_SIZE
        return hexToByteString(Numeric.toHexStringZeroPadded(new BigInteger(value), 16 * 2));
    }

    @Test
    public void testHashTx() throws Exception {
        BlockChain.TransactionHashTarget.Builder builder = BlockChain.TransactionHashTarget.newBuilder();
        builder.setChainId(180830);
        builder.setNonce(1);

        String to = "02bd4879f148079dee2bd096248ef3c4432ec1899681af4bdae2aa6a7451c72c7b";
        builder.setTo(getAddress(to));
        String from = "03349913aad7662ff63e3d200680a1773085184ccf34eca9022e76eabb53d55c98";
        builder.setFrom(getAddress(from));
        assertEquals(Numeric.byteArrayToHex(builder.getTo().toByteArray()), to);
        assertEquals(Numeric.byteArrayToHex(builder.getFrom().toByteArray()), from);

        builder.setTimestamp(1542702990085L);
        builder.setValue(getValue("1"));
        builder.setPayload(ByteString.EMPTY);
        builder.setTxType("transfer");
        String hash = Transaction.hashTx(builder.build());
        System.out.println(hash);
    }

    @Test
    public void testGetSendTransactionRequest() throws Exception {
        Account account = AccountUtils.loadAccount(new File("sampleAccount.json"));

        Rpc.SendTransactionRequest expected
                = Rpc.SendTransactionRequest.newBuilder()
                .setHashAlg(2)
                .setHash("e48c476c2fc1a21eee2a25a070bdb1f69788d6c0c3c84f7cfb84abae6868d9e3")
                .setChainId(181112)
                .setCryptoAlg(1)
                .setNonce(1)
                .setTimestamp(1544610216)
                //.setPayerSign(null)
                .setPayload("abc")
                .setSign("2bca8e8dc3faef143018284f3e9aba02af375c9f0693c44fab8ff14dd41c3aee029167bb360413175cc40fe80a40a35e9b10f96aedcea0b6a85289a9dc53083300")
                .setTo(account.getAddress())
                .setTxType("add_record")
                .setValue("0")
                .build();


        Rpc.SendTransactionRequest actual = Transaction.getSendTransactionRequest(account, "abc".getBytes());

        assertEquals(expected, actual);
    }
}
