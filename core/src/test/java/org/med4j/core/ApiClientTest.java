package org.med4j.core;

import static org.junit.Assert.*;

import io.reactivex.functions.Consumer;
import org.junit.Test;
import org.med4j.Med4J;
import org.med4j.core.protobuf.Rpc.*;

import io.reactivex.subscribers.TestSubscriber;

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
