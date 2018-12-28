import com.google.protobuf.util.JsonFormat;
import org.medibloc.panacea.account.Account;
import org.medibloc.panacea.account.AccountUtils;
import org.medibloc.panacea.core.HttpService;
import org.medibloc.panacea.core.Panacea;
import org.medibloc.panacea.core.protobuf.BlockChain;
import org.medibloc.panacea.core.protobuf.Rpc;
import org.medibloc.panacea.data.Data;
import org.medibloc.panacea.tx.Transaction;

import java.io.File;

public class Main {
    private static final String TESTNET_URL = "https://testnet-node.medibloc.org";
    private static final String ACCOUNT_REQUEST_TYPE_TAIL = "tail";

    private static final String ACCOUNT_FILE_PATH = "sample_en/sample_accounts";
    private static final String PASSWORD = "myPassWord123!";
    private static final String UPLOAD_DATA = "MyHealthDataForHashingAndUploading";

    public static void main(String[] args) throws Exception {
        System.out.println("Start panacea sample module.");


        /*** 1. create, save, and load account ***/

        // Create new account.
        // Use default option values if an option is not given.
        Account newAccount = AccountUtils.createAccount(PASSWORD, null);

        // Save the account to the given path.
        // The save file's format is "UTC--time--account_address.json".
        File savedFile = AccountUtils.saveAccount(newAccount, ACCOUNT_FILE_PATH);
        String savedFilePath = savedFile.getAbsolutePath();

        System.out.println("The created account is saved. Saved file : " + savedFilePath);

        // Load account information from the saved file.
        Account account = AccountUtils.loadAccount(savedFilePath);


        /*** 2. Upload data hash ***/

        // Create panacea client to access to MediBloc blockchain.
        // When communicating with the blockchain, Rpc(Remote Procedure Call) package's classes are used.
        Panacea panacea = Panacea.create(new HttpService(TESTNET_URL));

        // Get hash value of the data for uploading to the blockchain.
        byte[] dataHash = Data.hashRecord(UPLOAD_DATA);

        // Get account's current state on the blockchain.
        Rpc.GetAccountRequest accountRequest = Rpc.GetAccountRequest.newBuilder()
                .setAddress(account.getAddress())
                .setType(ACCOUNT_REQUEST_TYPE_TAIL)
                .build();
        Rpc.Account accountBCInfo = panacea.getAccount(accountRequest).send();
        long nextNonce = accountBCInfo.getNonce() + 1;

        // Get the chain ID of the blockchain. You can use a configuration file to save&load the chain ID.
        Rpc.MedState medState = panacea.getMedState().send();
        int chainId = medState.getChainId();

        // Generate a transaction for uploading to the blockchain. The generated transaction will be hashed and signed.
        BlockChain.TransactionHashTarget transactionHashTarget
                = Transaction.getAddRecordTransactionHashTarget(dataHash, account.getAddress(), nextNonce, chainId);

        // Hash transactionHashTarget and sign it using the private key. The given account and password is used for decrypt the private key.
        Rpc.SendTransactionRequest transactionRequest = Transaction.getSignedTransactionRequest(transactionHashTarget, account, PASSWORD);

        System.out.println("Upload transaction to blockchain.\ntransaction : " + JsonFormat.printer().print(transactionRequest));

        // Upload the transaction to blockchain and get the result.
        Rpc.TransactionHash resultHash = panacea.sendTransaction(transactionRequest).send();

        if (transactionRequest.getHash().equals(resultHash.getHash())) {
            System.out.println("The transaction is sent to blockchain transaction pool.");
        } else {
            throw new Exception("An error occurred while uploading the transaction.");
        }

        return;
    }
}
