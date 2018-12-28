package org.medibloc.panacea.core;

import org.medibloc.panacea.core.protobuf.Rpc.*;

public abstract class Panacea {
    public static Panacea create(ProtobufService service) {
        return new PanaceaImpl(service);
    }

    public abstract Request<Account> getAccount(GetAccountRequest request);

    public abstract Request<Block> getBlock(GetBlockRequest request);
    public abstract Request<Blocks> getBlocks(GetBlocksRequest request);

    public abstract Request<Candidates> getCandidates();
    public abstract Request<Dynasty> getDynasty();

    public abstract Request<MedState> getMedState();

    public abstract Request<Transactions> getPendingTransactions();
    public abstract Request<Transaction> getTransaction(String hash);
    public abstract Request<TransactionReceipt> getTransactionReceipt(String hash);

    public abstract Request<TransactionHash> sendTransaction(SendTransactionRequest request);

    public abstract Request<Health> healthCheck();
}
