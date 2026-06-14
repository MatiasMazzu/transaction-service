package io.github.matiasmazzu.transactionservice.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.matiasmazzu.transactionservice.application.port.TransactionRepository;
import io.github.matiasmazzu.transactionservice.domain.CycleChecker;
import io.github.matiasmazzu.transactionservice.domain.Transaction;
import io.github.matiasmazzu.transactionservice.domain.exception.CycleDetectedException;
import io.github.matiasmazzu.transactionservice.domain.exception.ParentNotFoundException;
import io.github.matiasmazzu.transactionservice.domain.exception.TransactionNotFoundException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

class TransactionServiceTest {

    private final FakeTransactionRepository repository = new FakeTransactionRepository();
    private final TransactionService service = new TransactionService(repository, new CycleChecker());

    private static Transaction tx(long id, Long parentId) {
        return new Transaction(id, BigDecimal.valueOf(100), "x", parentId);
    }

    private static Transaction tx(long id, String amount, Long parentId) {
        return new Transaction(id, new BigDecimal(amount), "x", parentId);
    }

    @Test
    void persistsNewRoot() {
        Transaction root = tx(1L, null);

        service.upsert(root);

        assertEquals(1, repository.size());
        assertEquals(root, repository.get(1L));
    }

    @Test
    void persistsNewChildWithExistingParent() {
        service.upsert(tx(1L, null));

        Transaction child = tx(2L, 1L);
        service.upsert(child);

        assertEquals(2, repository.size());
        assertEquals(child, repository.get(2L));
    }

    @Test
    void reapplyingSameUpsertIsIdempotent() {
        Transaction root = tx(1L, null);

        service.upsert(root);
        service.upsert(root);

        assertEquals(1, repository.size());
        assertEquals(root, repository.get(1L));
    }

    @Test
    void upsertReplacesExistingTransaction() {
        service.upsert(tx(1L, null));

        Transaction replacement = new Transaction(1L, BigDecimal.valueOf(999), "y", null);
        service.upsert(replacement);

        assertEquals(1, repository.size());
        assertEquals(replacement, repository.get(1L));
    }

    @Test
    void unknownParentThrowsAndPersistsNothing() {
        assertThrows(ParentNotFoundException.class, () -> service.upsert(tx(2L, 99L)));

        assertEquals(0, repository.size());
    }

    @Test
    void selfReferenceOnNewNodeThrowsCycleNotParentNotFound() {
        assertThrows(CycleDetectedException.class, () -> service.upsert(tx(5L, 5L)));

        assertEquals(0, repository.size());
    }

    @Test
    void selfReferenceOnExistingNodeThrowsCycle() {
        service.upsert(tx(5L, null));

        assertThrows(CycleDetectedException.class, () -> service.upsert(tx(5L, 5L)));

        assertEquals(1, repository.size());
        assertEquals(tx(5L, null), repository.get(5L));
    }

    @Test
    void closingACycleThrowsAndDoesNotPersistTheChange() {
        service.upsert(tx(1L, null));
        service.upsert(tx(2L, 1L));
        service.upsert(tx(3L, 2L));

        assertThrows(CycleDetectedException.class, () -> service.upsert(tx(1L, 3L)));

        assertEquals(3, repository.size());
        assertEquals(tx(1L, null), repository.get(1L));
    }

    @Test
    void validReParentingIsAccepted() {
        service.upsert(tx(1L, null));
        service.upsert(tx(2L, 1L));
        service.upsert(tx(3L, 2L));

        Transaction reparented = tx(3L, 1L);
        service.upsert(reparented);

        assertEquals(3, repository.size());
        assertEquals(reparented, repository.get(3L));
    }

    @Test
    void reproducesStatementSumExamples() {
        service.upsert(tx(10L, "5000", null));
        service.upsert(tx(11L, "10000", 10L));
        service.upsert(tx(12L, "5000", 11L));

        assertEquals(0, service.sum(10L).compareTo(new BigDecimal("20000")));
        assertEquals(0, service.sum(11L).compareTo(new BigDecimal("15000")));
    }

    @Test
    void sumsOwnAmountPlusAllDescendantsAcrossLevels() {
        service.upsert(tx(1L, "100", null));
        service.upsert(tx(2L, "10", 1L));
        service.upsert(tx(3L, "20", 1L));
        service.upsert(tx(4L, "5", 2L));

        assertEquals(0, service.sum(1L).compareTo(new BigDecimal("135")));
        assertEquals(0, service.sum(2L).compareTo(new BigDecimal("15")));
    }

    @Test
    void sumsInBigDecimalWithoutFloatingPointError() {
        service.upsert(tx(1L, "0.1", null));
        service.upsert(tx(2L, "0.2", 1L));

        assertEquals(0, service.sum(1L).compareTo(new BigDecimal("0.3")));
    }

    @Test
    void sumOfLeafIsItsOwnAmount() {
        service.upsert(tx(1L, "100", null));
        service.upsert(tx(2L, "42", 1L));

        assertEquals(0, service.sum(2L).compareTo(new BigDecimal("42")));
    }

    @Test
    void sumOfUnknownTransactionThrows() {
        assertThrows(TransactionNotFoundException.class, () -> service.sum(999L));
    }

    @Test
    @Timeout(10)
    void sumsDeepSubtreeWithoutStackOverflow() {
        int depth = 50_000;
        repository.save(new Transaction(1L, BigDecimal.ONE, "x", null));
        for (long id = 2; id <= depth; id++) {
            repository.save(new Transaction(id, BigDecimal.ONE, "x", id - 1));
        }

        assertEquals(0, service.sum(1L).compareTo(BigDecimal.valueOf(depth)));
    }

    private static final class FakeTransactionRepository implements TransactionRepository {
        private final Map<Long, Transaction> store = new HashMap<>();
        private final Map<Long, Set<Long>> childIdsByParent = new HashMap<>();

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
                childIdsByParent.computeIfAbsent(parentId, key -> new HashSet<>()).add(id);
            }
        }

        @Override
        public Map<Long, Transaction> findAll() {
            return store;
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
                children.add(store.get(childId));
            }
            return children;
        }

        int size() {
            return store.size();
        }

        Transaction get(long id) {
            return store.get(id);
        }
    }
}
