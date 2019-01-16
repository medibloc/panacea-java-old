package org.medibloc.panacea.core;

import com.google.protobuf.ByteString;
import org.junit.Test;
import org.medibloc.panacea.account.Account;
import org.medibloc.panacea.account.AccountUtils;
import org.medibloc.panacea.core.protobuf.BlockChain;
import org.medibloc.panacea.core.protobuf.Rpc;
import org.medibloc.panacea.data.Data;
import org.medibloc.panacea.tx.Transaction;
import org.medibloc.panacea.utils.Numeric;
import org.medibloc.panacea.utils.Strings;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;

public class TransactionTest {
    private static final int TESTNET_CHAIN_ID = 181112;

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

        builder.setValue(getValue("1"));
        builder.setPayload(ByteString.EMPTY);
        builder.setTxType("transfer");
        String hash = Transaction.hashTx(builder.build());
        System.out.println(hash);
    }

    // private key : 4627e66cd55fe54500bb0397663254564249b276f3bf81c21a3a06bd72dfcf74
    // public key : cb2bde8309a4bfde8e53be4e96a99082920fdccea0b5fddaf9d782d25a0e454f6cd4bbd19345cc3f3de58a8c11bb45e764bbacad507873e28e33c7f724bca1eb
    static final String[][] txSamples = {
            // {data, payload, hash, sign}
            {"0f1b3da26ff5a93c23363971bca8d9779521493c46ed8a9ab244e1269ee262c2", "0a20877bc8e087389cff5c7aac6eb8a40035c186c748346203f918dc8874d8399f29", "529e1182eacc7597a90366068ce31bf803e5c838251419ea28c8dabe284fb697", "2b17ef3916f0631b8a9834fd5a5e53fc0dd12ebb2e805d46cef973775977b185051516f103a8dacb61e12a07150047d5f78e34887735c3f5ffb7f5ed22f6f47700"},
            {"922757b52a6181bc5527a7b6d21b9b1cc29afb6922a521e8033c8c52f182e215", "0a20e366188b5079b59a5a942a86dee82751bb64ec08c0aef99f240c4f7209a5167a", "0830fcb6b30d4012192a6dd84edcb71a465a206dd679ae09be753e6da68aff2c", "7d2cb41e462f7cef3e7cb4c81f8046124608c7794a88105f95bd62c10187c5410f882f75c359e414692e966693ec9f6aeb65c557e6dea8e6ce869dbcc38f112600"},
            {"aa997794b14f9d9c2f3d9b8cec157f197a427258814250bac99337fd799370cd", "0a2063b537dd4db5e252984dce23bc58950253db3fa17ae9cc9cccf695ead6b79745", "826d8faed116a25a00f75a0b68883b6cce5fe835c6c28dcc9264d7b651455fb1", "3896278560bebd9545f5c4da4ef032b5e15f5acc0c8133d2aaceb0210f314a397f9b04684036922c1d1129e4586a8c3bf3cc4a6f97a3b0afa9a0bd486ba336b800"},
            {"822f84fe3ead72a38a367e8d3aafee15d0888b88311905483a26b77142006d6c", "0a20b2ffde51396f7ea46d13b4a6c2834d45109835bd12e568a5ed5af8369ab27e77", "bca946cd22dc8b3b0786a67152a21678ecff2e5558f4154f56cf2290f9402129", "caae94be8b3711388d71df9cd1fae1dc0b7548887a2db19d0fb77ac3077dbb825cdeace603e2da52210fa6dc5b385790dd40b6e2c79151872c57751a6b02107c01"},
            {"a7f007999ee52f2b0b52c9d5772bdbb180f39035b1dc6e3ee794ce3c79a4c16a", "0a204edfde3d1ad9320922c69fe9ef361b7cc5f558388acaddf78c73609c72cc1c05", "23fd1aa8cdedf34cf22f0e7da5e521b4da1d92b2f717d6e12c9ed2d81baebdac", "a12d5021cc21d70314d82a66661055e5d8bd8ba495cca53744c4c5f599655ff17830c3a63f6ae1a3e86a9e0a08672291194bad53831ee4eea8d1bb67028d238a00"},
            {"4642705d5e0425b1c37c1b78abbe8156844bbde9a20c50a6c080648105086a5e", "0a20927969455b0917db52b5848004f99fa79b9413ee6795bf982e9f7bb17137db68", "f4df44089c198590e9a19ae1425c16340c157c638f9bc27a40933f50a3a0813e", "0795c66037495ffef605a5190e94b042eeec23c3e59c859f201e71cc2d2168243dd2b2bf7ee9b8f51ac3a7721cfcdddefdc87d26c7815ae3d65ae6343802c14c00"},
            {"1684c19dfb1b3afb60be8e168529a8c95e1d874afbd940b80f8c146507605ddb", "0a20ff763bf51c02744c94c66540f0a956a15181f92c7fbf1d081ecdc6e0a01a8d28", "b0ddf4900a19edf4b66a8f1896ffea3f89297d54166c7c13d6375ac86b377782", "da2a34f377273964c09ae13e866021839d963bbfb5a0869d8c42c3988ec8ed8e3ee960038a0a8e572b0b9109bf9780bddfe9d05e8f70980d98fc9226e2cd9dd100"},
            {"f67929b553819291e6d7e536c2712e119c967cacdf50389f6e881ba597448d11", "0a20e041d80600e630b1270cc43a2082d72bdc6556f055a80560e2408dde874f9892", "ada9a5c187b5ddf9ca7c07307bbd5f910219bc339aff4803e487fbcaa8c05ada", "6c75793b01406f4a0b40b2529e3a1648291c5ac71a24a3c0db4a32f905da213d32379542439b8dc0ed56662006ff9e3e47e58f9de86001838a299b8f5ae552f100"},
            {"d870dea67c72df5640f488f92af3a0954d099f1b7e69cfd895566ca5aaeb5bc3", "0a20d6c30ffcf78cbc7d961a1cb68aa17dc3b86327fe1e97020cacba963edf838964", "1ac250952e9da527ba498baeead901b7f05bbca0962fa1d31980c1c99c779ad2", "be547cbf7cb5f45949b6e5b606cb8a26859a83e1f4ed9e0876a1f755f7e4a3656e355ae94ee13b227c3208afffa339eadd1e7f1af8765028acd6a90cd03034ec01"},
            {"1a2f4a1957186794a23cc03392a72c8cc409ec589d435bf664ee8378a0f5a265", "0a20c5647bdbc3113b20ff41b61a75ef3710dcd587a90493f5554c5756d9ea59344f", "ccde8e560846c828666b99eb3e6f8288a34c7e286b4f442302e941e29bd6d922", "e8c14b0c5743b619dc8a03a5f6e6d794f0f6b8aeb6184a2e3f4840c3a6950c127b2a6f2baa404b65c62f603bee10c8ed89f8b00a75ca92e5e0d36a4caaf8e27401"}
    };

    @Test
    public void testGetSendTransactionRequest() throws Exception {
        Account account = AccountUtils.loadAccount(AccountUtilsTest.SAMPLE_ACCOUNT_FILE_PATH);

        int idx = 0;
        for(String[] txSample: txSamples) {
            String data = txSample[0];
            String payload = txSample[1];
            String hash = txSample[2];
            String sign = txSample[3];

            Rpc.SendTransactionRequest expected
                    = Rpc.SendTransactionRequest.newBuilder()
                    .setHash(hash)
                    .setChainId(TESTNET_CHAIN_ID)
                    .setNonce(1)
                    //.setPayerSign(null)
                    .setPayload(payload)
                    .setSign(sign)
                    .setTo(Strings.zeros(33*2)) // default value
                    .setValue(Strings.zeros(16*2)) // default value
                    .setTxType("add_record")
                    .build();

            byte[] dataHash = Data.hashRecord(data);
            BlockChain.TransactionHashTarget transactionHashTarget
                    = Transaction.getAddRecordTransactionHashTarget(dataHash, account.getAddress(), 1, TESTNET_CHAIN_ID);
            Rpc.SendTransactionRequest actual = Transaction.getSignedTransactionRequest(transactionHashTarget, account, "sample");

            assertEquals(expected, actual);
            System.out.println("Pass test case for {index:" + idx++ + ", data:" + txSample[0] + "}");
        }
    }
}
