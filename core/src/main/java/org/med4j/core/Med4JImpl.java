package org.med4j.core;

import org.med4j.Med4J;
import org.med4j.core.protobuf.Rpc.*;

import java.io.IOException;
import java.util.HashMap;

public class Med4JImpl extends Med4J {
    private final ProtobufService protobufService;

    public Med4JImpl(ProtobufService protobufService) {
        this.protobufService = protobufService;
    }

    private HashMap<String, String> getHttpParams(String method, String path) {
        HashMap<String, String> methodAndPath = new HashMap<String, String>();
        methodAndPath.put(HttpService.METHOD_KEY, method);
        methodAndPath.put(HttpService.PATH_KEY, path);
        return methodAndPath;
    }

    @Override
    public Request<MedState> getMedState() {
        return protobufService.getRequest(
                NonParamRequest.newBuilder().build(),
                getHttpParams("GET", "/v1/node/medstate"),
                MedState.class);
    }

    @Override
    public Request<Transactions> getPendingTransactions() {
        return protobufService.getRequest(
                NonParamRequest.newBuilder().build(),
                getHttpParams("GET", "/v1/transactions/pending"),
                Transactions.class);
    }

    @Override
    public Request<Transaction> getTransaction(String hash) {
        return protobufService.getRequest(
                GetTransactionRequest.newBuilder().setHash(hash).build(),
                getHttpParams("GET", "/v1/transaction"),
                Transaction.class);
    }

    @Override
    public Request<TransactionReceipt> getTransactionReceipt(String hash) {
        return protobufService.getRequest(
                GetTransactionRequest.newBuilder().setHash(hash).build(),
                getHttpParams("GET", "/v1/transaction/receipt"),
                TransactionReceipt.class);
    }

    @Override
    public Request<TransactionHash> sendTransaction(SendTransactionRequest request) {
        return protobufService.getRequest(
                request,
                getHttpParams("POST", "/v1/transaction"),
                TransactionHash.class);
    }

    @Override
    public Request<Health> healthCheck() {
        return protobufService.getRequest(
                NonParamRequest.newBuilder().build(),
                getHttpParams("GET", "/v1/healthcheck"),
                Health.class);
    }

    @Override
    public Request<Account> getAccount(GetAccountRequest request) {
        return protobufService.getRequest(
                request,
                getHttpParams("GET", "/v1/account"),
                Account.class);
    }

    @Override
    public Request<Block> getBlock(GetBlockRequest request) {
        return protobufService.getRequest(
                request,
                getHttpParams("GET", "/v1/block"),
                Block.class);
    }

    @Override
    public Request<Blocks> getBlocks(GetBlocksRequest request) {
        return protobufService.getRequest(
                request,
                getHttpParams("GET", "/v1/blocks"),
                Blocks.class);
    }

    @Override
    public Request<Candidates> getCandidates() {
        return protobufService.getRequest(
                NonParamRequest.newBuilder().build(),
                getHttpParams("GET", "/v1/candidates"),
                Candidates.class);
    }

    @Override
    public Request<Dynasty> getDynasty() {
        return protobufService.getRequest(
                NonParamRequest.newBuilder().build(),
                getHttpParams("GET", "/v1/dynasty"),
                Dynasty.class);
    }

    @Override
    public void shutdown() {
        try {
            protobufService.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
