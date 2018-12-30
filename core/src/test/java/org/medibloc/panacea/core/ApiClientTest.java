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
    private static final String TESTNET_URL = "https://testnet-node.medibloc.org";
    private static final int TESTNET_CHAIN_ID = 181112;
    private static final String PASSWORD = "MediBlocPassWord123!";

    private Panacea getPanacea() {
        return Panacea.create(new HttpService(TESTNET_URL));
    }

    @Test
    public void testGetMedState() throws Exception {
        Panacea panacea = getPanacea();
        MedState response = panacea.getMedState().send();
        assertEquals(response.getChainId(), TESTNET_CHAIN_ID);
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
        Rpc.TransactionHash expected =  Rpc.TransactionHash.newBuilder()
                .setHash("ae22802a287a8c3e81076a3455b2f437b3f73f51601ca547e382114cd6cfa06c")
                .build();

        ECKeyPair ecKeyPair = new ECKeyPair(
                new BigInteger("9d10d24d7883c35f11dce98ba4da737f209808001748a595728dc326aa008b60", 16)
                , new BigInteger("7d31268680a3de375fb57d9fcf724fa95a7dfaa3a3381c910ccc24e1c0cb80ee8dd8acd6a4474e95d7ec81866f63e0b48651cdc9fd3fddf3316a8d18fe3bf8c0", 16));
        org.medibloc.panacea.account.Account account = AccountUtils.createAccount(PASSWORD, ecKeyPair, null);

        byte[] dataHash = Data.hashRecord("abc");
        BlockChain.TransactionHashTarget transactionHashTarget
                = Transaction.getAddRecordTransactionHashTarget(dataHash, account.getAddress(), 1, 181112);
        Rpc.SendTransactionRequest txReq = Transaction.getSignedTransactionRequest(transactionHashTarget, account, PASSWORD);

        Panacea panacea = getPanacea();
        Rpc.TransactionHash actual = panacea.sendTransaction(txReq).sendAsync().get();

        assertEquals(expected, actual);
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
