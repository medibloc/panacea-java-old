package org.medibloc.panacea.core;

import org.medibloc.panacea.core.protobuf.Rpc.*;

public interface Panacea {
    Request<Account> getAccount(GetAccountRequest request);

    Request<Block> getBlock(GetBlockRequest request);
    Request<Blocks> getBlocks(GetBlocksRequest request);

    Request<Candidates> getCandidates();
    Request<Dynasty> getDynasty();

    Request<MedState> getMedState();

    Request<Transactions> getPendingTransactions();
    Request<Transaction> getTransaction(String hash);
    Request<TransactionReceipt> getTransactionReceipt(String hash);

    Request<TransactionHash> sendTransaction(SendTransactionRequest request);

    Request<Health> healthCheck();
}
