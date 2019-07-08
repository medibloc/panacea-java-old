package org.medibloc.panacea.core;

import static org.junit.Assert.*;

import org.junit.Test;
import org.medibloc.panacea.account.AccountUtils;
import org.medibloc.panacea.core.protobuf.BlockChain;
import org.medibloc.panacea.core.protobuf.Rpc;
import org.medibloc.panacea.core.protobuf.Rpc.*;

import io.reactivex.subscribers.TestSubscriber;
import org.medibloc.panacea.crypto.ECKeyPair;
import org.medibloc.panacea.data.Data;
import org.medibloc.panacea.tx.Transaction;

import java.math.BigInteger;

public class ApiClientTest {
    private static final String TESTNET_URL = "https://stg-testnet-node.medibloc.org";
    private static final int TESTNET_CHAIN_ID = 181112;
    private static final int STG_TESTNET_CHAIN_ID = 181228;
    private static final String PASSWORD = "MediBlocPassWord123!";
    private static final String ACCOUNT_REQUEST_TYPE_TAIL = "tail";

    private Panacea getPanacea() {
        return Panacea.create(new HttpService(TESTNET_URL));
    }

    @Test
    public void testGetMedState() throws Exception {
        Panacea panacea = getPanacea();
        MedState response = panacea.getMedState().send();
        assertEquals(response.getChainId(), STG_TESTNET_CHAIN_ID);
    }

    @Test
    public void testGetMedStateFlowable() throws Exception {
        Panacea panacea = getPanacea();
        TestSubscriber<MedState> subscriber = new TestSubscriber<MedState>();
        panacea.getMedState().flowable()
                .subscribe(subscriber);
        subscriber.assertComplete();
        subscriber.assertNoErrors();
    }

    @Test
    public void testGetAccount() throws Exception {
        Account.Builder expectedResBuilder = Account.newBuilder();
        expectedResBuilder.setAddress("02dc01a49f2867a44e7a0fd08fb4a3e5a3c628d35ac6c444b1acc48617b4158458");
        expectedResBuilder.setBalance("0");
        expectedResBuilder.setStaking("0");
        expectedResBuilder.setPoints("0");
        expectedResBuilder.setUnstaking("0");

        GetAccountRequest.Builder reqBuilder = GetAccountRequest.newBuilder();
        reqBuilder.setAddress("02dc01a49f2867a44e7a0fd08fb4a3e5a3c628d35ac6c444b1acc48617b4158458");
        reqBuilder.setType("tail");

        Account expected = expectedResBuilder.build();
        Account actual = getPanacea().getAccount(reqBuilder.build()).sendAsync().get();

        assertEquals(expected, actual);
    }

    @Test
    public void testGetCandidates() throws Exception {
        Panacea panacea = getPanacea();
        panacea.getCandidates().send();
    }

    @Test
    public void testGetDynasty() throws Exception {
        Panacea panacea = getPanacea();
        panacea.getDynasty().send();
    }

    @Test
    public void testSendTransaction() throws Exception {
        ECKeyPair ecKeyPair = new ECKeyPair(
                new BigInteger("4da8bc28a095870433d8a7d57ca140d6132e722f177c9a94f70a6963b4b8f708", 16)
                , new BigInteger("e34caca7b7653eb6cbb64cdd9e7c691545cbbe002a5ef9ed86e71577d9c7c2960da413ededc3216df47f27ba6d46babe0ba54ca35d682182d26a6c6aa63f7930", 16));
        org.medibloc.panacea.account.Account account = AccountUtils.createAccount(PASSWORD, ecKeyPair, null);

        byte[] dataHash = Data.hashRecord("abc");

        Panacea panacea = getPanacea();

        Rpc.GetAccountRequest accountRequest = Rpc.GetAccountRequest.newBuilder()
                .setAddress(account.getAddress())
                .setType(ACCOUNT_REQUEST_TYPE_TAIL)
                .build();
        Rpc.Account accountBCInfo = panacea.getAccount(accountRequest).send();
        long nextNonce = accountBCInfo.getNonce() + 1;

        BlockChain.TransactionHashTarget transactionHashTarget
                = Transaction.getAddRecordTransactionHashTarget(dataHash, account.getAddress(), nextNonce, STG_TESTNET_CHAIN_ID);
        Rpc.SendTransactionRequest txReq = Transaction.getSignedTransactionRequest(transactionHashTarget, account, PASSWORD);

        Rpc.TransactionHash txHash = panacea.sendTransaction(txReq).sendAsync().get();
        System.out.println("tx hash : " + txHash.getHash());

        Thread.sleep(5000);

        Rpc.Transaction registeredTx = panacea.getTransaction(txHash.getHash()).send();

        assertEquals(txReq.getPayload(), registeredTx.getPayload());
    }

    @Test
    public void testHealthCheck() throws Exception {
        Panacea panacea = getPanacea();
        panacea.healthCheck().send();
    }

    @Test
    public void testHeathCheckAsync() throws Exception {
        Panacea panacea = getPanacea();
        Health health = panacea.healthCheck().sendAsync().get();
        assertTrue(health.getOk());
    }

    @Test
    public void testHealthCheckFlowable() throws Exception {
        Panacea panacea = getPanacea();
        TestSubscriber<Health> subscriber = new TestSubscriber<Health>();
        panacea.healthCheck().flowable().subscribe(subscriber);

        subscriber.assertComplete();
        subscriber.assertNoErrors();
        subscriber.assertResult(Health.newBuilder().setOk(true).build());
    }
}
