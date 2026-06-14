package io.github.matiasmazzu.transactionservice.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.matiasmazzu.transactionservice.application.port.TransactionRepository;
import io.github.matiasmazzu.transactionservice.domain.CycleChecker;
import io.github.matiasmazzu.transactionservice.domain.Transaction;
import io.github.matiasmazzu.transactionservice.domain.exception.CycleDetectedException;
import io.github.matiasmazzu.transactionservice.domain.exception.ParentNotFoundException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TransactionServiceTest {

    private final FakeTransactionRepository repository = new FakeTransactionRepository();
    private final TransactionService service = new TransactionService(repository, new CycleChecker());

    private static Transaction tx(long id, Long parentId) {
        return new Transaction(id, BigDecimal.valueOf(100), "x", parentId);
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

    private static final class FakeTransactionRepository implements TransactionRepository {
        private final Map<Long, Transaction> store = new HashMap<>();

        @Override
        public void save(Transaction transaction) {
            store.put(transaction.transactionId(), transaction);
        }

        @Override
        public Map<Long, Transaction> findAll() {
            return store;
        }

        int size() {
            return store.size();
        }

        Transaction get(long id) {
            return store.get(id);
        }
    }
}
