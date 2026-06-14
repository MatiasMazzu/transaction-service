package io.github.matiasmazzu.transactionservice.adapter.out.persistence;

import io.github.matiasmazzu.transactionservice.application.port.TransactionRepository;
import io.github.matiasmazzu.transactionservice.domain.Transaction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryTransactionRepository implements TransactionRepository {

    private final ConcurrentHashMap<Long, Transaction> store = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Set<Long>> childIdsByParent = new ConcurrentHashMap<>();

    @Override
    public void save(Transaction transaction) {
        long id = transaction.transactionId();
        Transaction previous = store.put(id, transaction);
        if (previous != null && previous.parentId() != null) {
            Set<Long> oldSiblings = childIdsByParent.get(previous.parentId());
            if (oldSiblings != null) {
                oldSiblings.remove(id);
            }
        }
        Long parentId = transaction.parentId();
        if (parentId != null) {
            childIdsByParent.computeIfAbsent(parentId, key -> ConcurrentHashMap.newKeySet()).add(id);
        }
    }

    @Override
    public Map<Long, Transaction> findAll() {
        return Collections.unmodifiableMap(store);
    }

    @Override
    public Optional<Transaction> findById(long transactionId) {
        return Optional.ofNullable(store.get(transactionId));
    }

    @Override
    public Collection<Transaction> findChildren(long parentId) {
        Set<Long> childIds = childIdsByParent.getOrDefault(parentId, Set.of());
        List<Transaction> children = new ArrayList<>(childIds.size());
        for (Long childId : childIds) {
            Transaction child = store.get(childId);
            if (child != null) {
                children.add(child);
            }
        }
        return children;
    }

    @Override
    public Collection<Long> findByType(String type) {
        List<Long> ids = new ArrayList<>();
        for (Transaction transaction : store.values()) {
            if (transaction.type().equals(type)) {
                ids.add(transaction.transactionId());
            }
        }
        return ids;
    }
}
