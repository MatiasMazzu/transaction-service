package io.github.matiasmazzu.transactionservice.application;

import io.github.matiasmazzu.transactionservice.application.port.TransactionRepository;
import io.github.matiasmazzu.transactionservice.domain.CycleChecker;
import io.github.matiasmazzu.transactionservice.domain.Transaction;
import io.github.matiasmazzu.transactionservice.domain.exception.CycleDetectedException;
import io.github.matiasmazzu.transactionservice.domain.exception.ParentNotFoundException;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class TransactionService {

    private final TransactionRepository repository;
    private final CycleChecker cycleChecker;
    private final ReentrantLock lock = new ReentrantLock();

    public TransactionService(TransactionRepository repository, CycleChecker cycleChecker) {
        this.repository = repository;
        this.cycleChecker = cycleChecker;
    }

    public void upsert(Transaction transaction) {
        long id = transaction.transactionId();
        Long proposedParentId = transaction.parentId();
        lock.lock();
        try {
            Map<Long, Transaction> graph = repository.findAll();
            if (cycleChecker.wouldFormCycle(graph, id, proposedParentId)) {
                throw new CycleDetectedException(
                        "cycle detected assigning parent " + proposedParentId + " to transaction " + id);
            }
            if (proposedParentId != null && !graph.containsKey(proposedParentId)) {
                throw new ParentNotFoundException(
                        "parent " + proposedParentId + " not found for transaction " + id);
            }
            repository.save(transaction);
        } finally {
            lock.unlock();
        }
    }
}
