import com.google.protobuf.util.JsonFormat;
import org.med4j.Med4J;
import org.med4j.account.Account;
import org.med4j.account.AccountUtils;
import org.med4j.core.HttpService;
import org.med4j.core.protobuf.BlockChain;
import org.med4j.core.protobuf.Rpc;
import org.med4j.data.Data;
import org.med4j.tx.Transaction;

import java.io.File;

public class Main {
    private static final String TESTNET_URL = "https://testnet-node.medibloc.org";
    private static final String ACCOUNT_REQUEST_TYPE_TAIL = "tail";

    private static final String ACCOUNT_FILE_PATH = "sample_ko/sample_accounts";
    private static final String PASSWORD = "myPassWord123!";
    private static final String UPLOAD_DATA = "MyHealthDataForHashingAndUploading";

    public static void main(String[] args) throws Exception {
        System.out.println("Med4j sample 프로그램을 실행합니다.");


        /*** 1. account 생성, 저장 및 불러오기 ***/

        // 새로운 account 를 생성합니다.
        // 옵션이 주어지지 않으면 기본 옵션 값이 설정 됩니다.
        Account newAccount = AccountUtils.createAccount(PASSWORD, null);

        // 생성한 account 를 주어진 경로에 저장합니다.
        // 저장되는 파일명은 "UTC--시간--account주소.json" 형식입니다.
        File savedFile = AccountUtils.saveAccount(newAccount, ACCOUNT_FILE_PATH);
        String savedFilePath = savedFile.getAbsolutePath();

        System.out.println("생성한 account 를 저장 하였습니다. 파일명 : " + savedFilePath);

        // 저장된 account 파일로부터 account 정보를 읽습니다.
        Account account = AccountUtils.loadAccount(savedFilePath);


        /*** 2. data hash 업로드 ***/

        // Blockchain 에 접근하기 위한 med4j client 를 생성 합니다.
        // med4j client 를 이용하여 Blockchain 과 통신 할 때에는 Rpc(Remote Procedure Call) 패키지 내의 클래스가 사용 됩니다.
        Med4J med4J = Med4J.create(new HttpService(TESTNET_URL));

        // Blockchain 에 업로드 할 data hash 값을 구합니다.
        byte[] dataHash = Data.hashRecord(UPLOAD_DATA);

        // Blockchain 에서 account 의 현재 정보를 조회 합니다.
        Rpc.GetAccountRequest accountRequest = Rpc.GetAccountRequest.newBuilder()
                .setAddress(account.getAddress())
                .setType(ACCOUNT_REQUEST_TYPE_TAIL)
                .build();
        Rpc.Account accountBCInfo = med4J.getAccount(accountRequest).send();
        long nextNonce = accountBCInfo.getNonce() + 1;

        // Blockchain 의 chainId 를 조회 합니다. 또는, 환경 설정 파일에 저장한 chainId 를 이용 할 수도 있습니다.
        Rpc.MedState medState = med4J.getMedState().send();
        int chainId = medState.getChainId();

        // Blockchain 에 등록 할 transaction 을 생성 합니다. 생성된 transaction 은 hash 및 sign 의 대상이 됩니다.
        BlockChain.TransactionHashTarget transactionHashTarget
                = Transaction.getAddRecordTransactionHashTarget(dataHash, account.getAddress(), nextNonce, chainId);

        // transactionHashTarget 을 hash 하여 transaction 의 고유 hash 값을 생성 하고, 주어진 account 와 비밀번호를 이용하여 생성된 hash 값을 개인키로 sign 합니다.
        Rpc.SendTransactionRequest transactionRequest = Transaction.getSignedTransactionRequest(transactionHashTarget, account, PASSWORD);

        System.out.println("블록체인에 새로운 transaction 을 업로드 합니다.\ntransaction : " + JsonFormat.printer().print(transactionRequest));

        // Blockchain 에 transaction 을 업로드 하고 결과를 반환 받습니다.
        Rpc.TransactionHash resultHash = med4J.sendTransaction(transactionRequest).send();

        if (transactionRequest.getHash().equals(resultHash.getHash())) {
            System.out.println("요청한 transaction 이 Blockchain transaction pool 에 등록 되었습니다.");
        } else {
            throw new Exception("transaction 업로드 중 오류가 발생 하였습니다.");
        }

        return;
    }
}
