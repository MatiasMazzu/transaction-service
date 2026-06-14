package io.github.matiasmazzu.transactionservice.application;

import io.github.matiasmazzu.transactionservice.application.port.TransactionRepository;
import io.github.matiasmazzu.transactionservice.domain.CycleChecker;
import io.github.matiasmazzu.transactionservice.domain.Transaction;
import io.github.matiasmazzu.transactionservice.domain.exception.CycleDetectedException;
import io.github.matiasmazzu.transactionservice.domain.exception.ParentNotFoundException;
import io.github.matiasmazzu.transactionservice.domain.exception.TransactionNotFoundException;
import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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

    public BigDecimal sum(long transactionId) {
        Transaction root = repository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException("transaction " + transactionId + " not found"));
        BigDecimal total = BigDecimal.ZERO;
        Set<Long> visited = new HashSet<>();
        Deque<Transaction> stack = new ArrayDeque<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            Transaction current = stack.pop();
            if (!visited.add(current.transactionId())) {
                continue;
            }
            total = total.add(current.amount());
            for (Transaction child : repository.findChildren(current.transactionId())) {
                stack.push(child);
            }
        }
        return total;
    }
}
