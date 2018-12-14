package org.med4j.core;

import com.google.protobuf.ByteString;
import org.junit.Test;
import org.med4j.account.Account;
import org.med4j.account.AccountUtils;
import org.med4j.core.protobuf.BlockChain;
import org.med4j.core.protobuf.Rpc;
import org.med4j.tx.Transaction;
import org.med4j.utils.Numeric;
import org.med4j.utils.Strings;

import java.io.File;
import java.math.BigInteger;

import static org.junit.Assert.assertEquals;

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

    // private key : 4627e66cd55fe54500bb0397663254564249b276f3bf81c21a3a06bd72dfcf74
    // public key : cb2bde8309a4bfde8e53be4e96a99082920fdccea0b5fddaf9d782d25a0e454f6cd4bbd19345cc3f3de58a8c11bb45e764bbacad507873e28e33c7f724bca1eb
    static final String[][] txSamples = {
            // {data, payload, hash, sign}
            {"0f1b3da26ff5a93c23363971bca8d9779521493c46ed8a9ab244e1269ee262c2", "0a20877bc8e087389cff5c7aac6eb8a40035c186c748346203f918dc8874d8399f29", "813d3f751ce604720bd05b82a4b684d9d21087ebd83228a60089a07953ca7062", "fdc1a3ec79ebe80062002c947994b8031f1aa00af0e7a2ff155365aaf176f1a6642aa68d9ae1cf13a73f316de0f978b22dc9c0c483bb8f15065894d35685c87500"},
            {"922757b52a6181bc5527a7b6d21b9b1cc29afb6922a521e8033c8c52f182e215", "0a20e366188b5079b59a5a942a86dee82751bb64ec08c0aef99f240c4f7209a5167a", "a8b2b8afa3de83e536682e1e9918a45de190b9cca51a30cb162f597d678ee596", "316186dab92c822520ef0220725faafa61ba9ec9fe1cbed3d2cfc1e7bbd87a2f78bf4f608840128943df6990006bf4e4cc8fb7aaba0f12f39788269071cff3d801"},
            {"aa997794b14f9d9c2f3d9b8cec157f197a427258814250bac99337fd799370cd", "0a2063b537dd4db5e252984dce23bc58950253db3fa17ae9cc9cccf695ead6b79745", "29f3631e0ed636859791ddd7a8a145467d5eda5e2a08411d8029cb50ffcab26f", "b4ab73c379e12f42844598430cb506ca1d4eeb501f2ff2524602742f6682b0867fc431e5f72d9425ef43ac09002b4fd9f8a0396d02584d5b4ea25e1e58f7f7d701"},
            {"822f84fe3ead72a38a367e8d3aafee15d0888b88311905483a26b77142006d6c", "0a20b2ffde51396f7ea46d13b4a6c2834d45109835bd12e568a5ed5af8369ab27e77", "136844b0a1e9501474a2611692217eeb71157dae19cf121b6a01fa65bc82af41", "ed612635712edc1a0abe13dfca5b1b697736d9592120541a52d507314556ef527e8ee1a816f1a90c447fc2efbccde45de29b15b0a0b1f7dfa46d15e83d2e164601"},
            {"a7f007999ee52f2b0b52c9d5772bdbb180f39035b1dc6e3ee794ce3c79a4c16a", "0a204edfde3d1ad9320922c69fe9ef361b7cc5f558388acaddf78c73609c72cc1c05", "4571ec301295d134264908f53ad6378d6f6cd1bb3e1865ecb7aa2badb782365a", "2673f71843479b2f9cce5615752d940acba9452b598054d1dba36b30a905dabf57c7740d8a9a29b2fcebaea4e45843751a62018c0b6e0f5958f9ece977ef541800"},
            {"4642705d5e0425b1c37c1b78abbe8156844bbde9a20c50a6c080648105086a5e", "0a20927969455b0917db52b5848004f99fa79b9413ee6795bf982e9f7bb17137db68", "1421214eedc07e4037fbd466b00318a7912a47fda85bb75c37afee3271c10a45", "15966a0085e4155ffd7f4fbeb6cee88e380247f3a5bb40f920a1c6a863dfc02d19d561bc501036c466dd05b9f514b0f06898c85d2edf399835f6ea51de0d1d4501"},
            {"1684c19dfb1b3afb60be8e168529a8c95e1d874afbd940b80f8c146507605ddb", "0a20ff763bf51c02744c94c66540f0a956a15181f92c7fbf1d081ecdc6e0a01a8d28", "bd7b4ac6b0160235429d6aaf52c3b793f76cbc62a7b3dbc4b6d3eb1e75ba6972", "6244babd08b5c0bfb84c9b14e136b2e911e4b56192fb71047cf93bb824f7f74963dcf780e2a5ba740286e9f0bf5c36fe432353850f0e20f092de0f507464729300"},
            {"f67929b553819291e6d7e536c2712e119c967cacdf50389f6e881ba597448d11", "0a20e041d80600e630b1270cc43a2082d72bdc6556f055a80560e2408dde874f9892", "06e1b63272a0a3ef60fcee37fec483aa8ed006ad9d2892f5c7bdeabd9fc0f022", "7aed8c9f9185687ee2f666385588ef504e4edefdbe83a2a27ee354bfda4e71e8493ce4e8ff02a933c805b307b79634439bee21d07fed0407b528788052a1e51800"},
            {"d870dea67c72df5640f488f92af3a0954d099f1b7e69cfd895566ca5aaeb5bc3", "0a20d6c30ffcf78cbc7d961a1cb68aa17dc3b86327fe1e97020cacba963edf838964", "7ee45df7c90258c65787297a81c272e1e4d3a17e5646b4140e1721baad4b70de", "5a2dfdd97d30af26760d5dddd074061583c12fbda9b42837beb25cded2ba57152cc9df16c1688abd605edf5a812fdbc7a4db680e7b21c8bc384e7033056556ac01"},
            {"1a2f4a1957186794a23cc03392a72c8cc409ec589d435bf664ee8378a0f5a265", "0a20c5647bdbc3113b20ff41b61a75ef3710dcd587a90493f5554c5756d9ea59344f", "422bb00207f08ab96f265715b26ef74dbab623bfd4f8f89170a66b91fc1bba5f", "819cb4a9fdeebff39d65cb1bcabdb00894cd2fce990031eb497521ad80438cfc5ea1a04e9c9368bb7b1cb056e10233d2d071df0e46d57faf619153d13ed04ac501"}
    };

    @Test
    public void testGetSendTransactionRequest() throws Exception {
        Account account = AccountUtils.loadAccount(new File("sampleAccount.json"));

        for(String[] txSample: txSamples) {
            String data = txSample[0];
            String payload = txSample[1];
            String hash = txSample[2];
            String sign = txSample[3];

            Rpc.SendTransactionRequest expected
                    = Rpc.SendTransactionRequest.newBuilder()
                    .setHashAlg(2)
                    .setHash(hash)
                    .setChainId(181112)
                    .setCryptoAlg(1)
                    .setNonce(1)
                    .setTimestamp(1540000000)
                    //.setPayerSign(null)
                    .setPayload(payload)
                    .setSign(sign)
                    .setTo(Strings.zeros(33*2)) // default value
                    .setValue(Strings.zeros(16*2)) // default value
                    .setTxType("add_record")
                    .build();

            Rpc.SendTransactionRequest actual = Transaction.getSendTransactionRequest(data.getBytes(), account, "sample", 1540000000, 1, 181112);

            assertEquals(expected, actual);
        }
    }
}
