import com.google.protobuf.util.JsonFormat;
import org.med4j.Med4J;
import org.med4j.account.Account;
import org.med4j.account.AccountUtils;
import org.med4j.core.HttpService;
import org.med4j.core.protobuf.Rpc;
import org.med4j.tx.Transaction;

import java.io.File;

public class Main {
    private static final String TESTNET_URL = "https://testnet-node.medibloc.org";

    public static void main(String[] args) throws Exception {
        System.out.println("Med4j sample 프로그램을 실행합니다.");


        /*** 1. account 생성, 저장 및 불러오기 ***/

        // 새로운 account 를 생성합니다.
        // 옵션이 주어지지 않으면 기본 옵션 값이 설정 됩니다.
        Account newAccount = AccountUtils.createAccount("myPassWord123!", null);

        // 생성한 account 를 주어진 경로에 저장합니다.
        // 저장되는 파일명은 "UTC--시간--account주소.json" 형식입니다.
        File savedFile = AccountUtils.saveAccount(newAccount, "sample_ko/sample_accounts");
        String savedFilePath = savedFile.getAbsolutePath();

        System.out.println("생성한 account 를 저장 하였습니다. 파일명 : " + savedFilePath);

        // 저장된 account 파일로부터 account 정보를 읽습니다.
        Account account = AccountUtils.loadAccount(savedFilePath);


        /*** 2. data hash 업로드 ***/

        // BlockChain 에 접근하기 위한 med4j client 를 생성 합니다.
        Med4J med4J = Med4J.create(new HttpService(TESTNET_URL));

        // BlockChain 의 chainId 를 조회 합니다. 또는, 환경 설정 파일에 저장한 chainId 를 이용 할 수도 있습니다.
        Rpc.MedState medState = med4J.getMedState().send(); // 동기 호출
        int chainId = medState.getChainId();

        // BlockChain 에서 account 의 현재 정보를 조회 합니다.
        Rpc.GetAccountRequest accountRequest = Rpc.GetAccountRequest.newBuilder()
                .setAddress(account.getAddress())
                .setType("tail")
                .build();
        Rpc.Account accountBCInfo = med4J.getAccount(accountRequest).sendAsync().get(); // 비동기 호출

        // BlockChain 에 등록 할 transaction request 를 생성 합니다.
        // 내부적으로 data hash 값을 생성하고, 주어진 account 와 비밀번호를 이용하여 생성된 hash 값을 개인키로 sign 합니다.
        long nextNonce = accountBCInfo.getNonce() + 1;
        Rpc.SendTransactionRequest transactionRequest = Transaction.getSendTransactionRequest("MyHealthDataForHashingAndUploading", account, "myPassWord123!", nextNonce, chainId);

        System.out.println("블록체인에 새로운 transaction 을 업로드 합니다.\ntransaction : " + JsonFormat.printer().print(transactionRequest));

        // BlockChain 에 transaction 을 업로드 하고 결과를 반환 받습니다.
        Rpc.TransactionHash resultHash = med4J.sendTransaction(transactionRequest).sendAsync().get(); // 비동기 호출

        if (transactionRequest.getHash().equals(resultHash.getHash())) {
            System.out.println("요청한 transaction 이 BlockChain transaction pool 에 등록 되었습니다.");
        } else {
            throw new Exception("transaction 업로드 중 오류가 발생 하였습니다.");
        }

        return;
    }
}
