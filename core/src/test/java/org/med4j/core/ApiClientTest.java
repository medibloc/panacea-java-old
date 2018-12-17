package org.med4j.core;

import static org.junit.Assert.*;

import org.junit.Test;
import org.med4j.Med4J;
import org.med4j.account.AccountUtils;
import org.med4j.core.protobuf.Rpc;
import org.med4j.core.protobuf.Rpc.*;

import io.reactivex.subscribers.TestSubscriber;
import org.med4j.tx.Transaction;

import java.io.File;

public class ApiClientTest {
    private static final String TESTNET_URL = "https://testnet-node.medibloc.org";
    private static final int TESTNET_CHAIN_ID = 181112;

    private Med4J getMed4J() {
        return Med4J.create(new HttpService(TESTNET_URL));
    }

    @Test
    public void testGetMedState() throws Exception {
        Med4J med4J = getMed4J();
        MedState response = med4J.getMedState().send();
        assertEquals(response.getChainId(), TESTNET_CHAIN_ID);
    }

    @Test
    public void testGetMedStateFlowable() throws Exception {
        Med4J med4J = getMed4J();
        TestSubscriber<MedState> subscriber = new TestSubscriber<MedState>();
        med4J.getMedState().flowable()
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
        Account actual = getMed4J().getAccount(reqBuilder.build()).sendAsync().get();

        assertEquals(expected, actual);
    }

    @Test
    public void testGetCandidates() throws Exception {
        Med4J med4J = getMed4J();
        med4J.getCandidates().send();
    }

    @Test
    public void testGetDynasty() throws Exception {
        Med4J med4J = getMed4J();
        med4J.getDynasty().send();
    }

    @Test
    public void testSendTransaction() throws Exception {
        Rpc.TransactionHash expected =  Rpc.TransactionHash.newBuilder()
                .setHash("ae22802a287a8c3e81076a3455b2f437b3f73f51601ca547e382114cd6cfa06c")
                .build();

        org.med4j.account.Account account = AccountUtils.loadAccount(new File("sampleAccount.json"));
        Rpc.SendTransactionRequest txReq = Transaction.getSendTransactionRequest("abc".getBytes(), account, "sample", 1540000000, 1, 181112);

        Med4J med4J = getMed4J();
        Rpc.TransactionHash actual = med4J.sendTransaction(txReq).sendAsync().get();

        assertEquals(expected, actual);
    }

    @Test
    public void testHealthCheck() throws Exception {
        Med4J med4J = getMed4J();
        med4J.healthCheck().send();
    }

    @Test
    public void testHeathCheckAsync() throws Exception {
        Med4J med4J = getMed4J();
        Health health = med4J.healthCheck().sendAsync().get();
        assertTrue(health.getOk());
    }

    @Test
    public void testHealthCheckFlowable() throws Exception {
        Med4J med4J = getMed4J();
        TestSubscriber<Health> subscriber = new TestSubscriber<Health>();
        med4J.healthCheck().flowable().subscribe(subscriber);

        subscriber.assertComplete();
        subscriber.assertNoErrors();
        subscriber.assertResult(Health.newBuilder().setOk(true).build());
    }
}
