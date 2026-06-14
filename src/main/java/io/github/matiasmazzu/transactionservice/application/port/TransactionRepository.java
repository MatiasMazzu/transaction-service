package io.github.matiasmazzu.transactionservice.application.port;

import io.github.matiasmazzu.transactionservice.domain.Transaction;
import java.util.Map;

public interface TransactionRepository {

    void save(Transaction transaction);

    Map<Long, Transaction> findAll();
}
