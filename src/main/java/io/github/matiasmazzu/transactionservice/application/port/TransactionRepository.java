package io.github.matiasmazzu.transactionservice.application.port;

import io.github.matiasmazzu.transactionservice.domain.Transaction;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface TransactionRepository {

    void save(Transaction transaction);

    Map<Long, Transaction> findAll();

    Optional<Transaction> findById(long transactionId);

    Collection<Transaction> findChildren(long parentId);
}
