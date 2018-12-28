package org.medibloc.panacea.core;

import org.medibloc.panacea.core.protobuf.Rpc;

import java.io.IOException;
import java.util.HashMap;

public class PanaceaImpl extends Panacea {
    private final ProtobufService protobufService;

    public PanaceaImpl(ProtobufService protobufService) {
        this.protobufService = protobufService;
    }

    private HashMap<String, String> getHttpParams(String method, String path) {
        HashMap<String, String> methodAndPath = new HashMap<String, String>();
        methodAndPath.put(HttpService.METHOD_KEY, method);
        methodAndPath.put(HttpService.PATH_KEY, path);
        return methodAndPath;
    }

    @Override
    public Request<Rpc.MedState> getMedState() {
        return protobufService.getRequest(
                Rpc.NonParamRequest.newBuilder().build(),
                getHttpParams("GET", "/v1/node/medstate"),
                Rpc.MedState.class);
    }

    @Override
    public Request<Rpc.Transactions> getPendingTransactions() {
        return protobufService.getRequest(
                Rpc.NonParamRequest.newBuilder().build(),
                getHttpParams("GET", "/v1/transactions/pending"),
                Rpc.Transactions.class);
    }

    @Override
    public Request<Rpc.Transaction> getTransaction(String hash) {
        return protobufService.getRequest(
                Rpc.GetTransactionRequest.newBuilder().setHash(hash).build(),
                getHttpParams("GET", "/v1/transaction"),
                Rpc.Transaction.class);
    }

    @Override
    public Request<Rpc.TransactionReceipt> getTransactionReceipt(String hash) {
        return protobufService.getRequest(
                Rpc.GetTransactionRequest.newBuilder().setHash(hash).build(),
                getHttpParams("GET", "/v1/transaction/receipt"),
                Rpc.TransactionReceipt.class);
    }

    @Override
    public Request<Rpc.TransactionHash> sendTransaction(Rpc.SendTransactionRequest request) {
        return protobufService.getRequest(
                request,
                getHttpParams("POST", "/v1/transaction"),
                Rpc.TransactionHash.class);
    }

    @Override
    public Request<Rpc.Health> healthCheck() {
        return protobufService.getRequest(
                Rpc.NonParamRequest.newBuilder().build(),
                getHttpParams("GET", "/v1/healthcheck"),
                Rpc.Health.class);
    }

    @Override
    public Request<Rpc.Account> getAccount(Rpc.GetAccountRequest request) {
        return protobufService.getRequest(
                request,
                getHttpParams("GET", "/v1/account"),
                Rpc.Account.class);
    }

    @Override
    public Request<Rpc.Block> getBlock(Rpc.GetBlockRequest request) {
        return protobufService.getRequest(
                request,
                getHttpParams("GET", "/v1/block"),
                Rpc.Block.class);
    }

    @Override
    public Request<Rpc.Blocks> getBlocks(Rpc.GetBlocksRequest request) {
        return protobufService.getRequest(
                request,
                getHttpParams("GET", "/v1/blocks"),
                Rpc.Blocks.class);
    }

    @Override
    public Request<Rpc.Candidates> getCandidates() {
        return protobufService.getRequest(
                Rpc.NonParamRequest.newBuilder().build(),
                getHttpParams("GET", "/v1/candidates"),
                Rpc.Candidates.class);
    }

    @Override
    public Request<Rpc.Dynasty> getDynasty() {
        return protobufService.getRequest(
                Rpc.NonParamRequest.newBuilder().build(),
                getHttpParams("GET", "/v1/dynasty"),
                Rpc.Dynasty.class);
    }

    public void shutdown() {
        try {
            protobufService.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
