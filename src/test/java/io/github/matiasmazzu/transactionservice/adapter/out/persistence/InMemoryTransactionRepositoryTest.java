package io.github.matiasmazzu.transactionservice.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.matiasmazzu.transactionservice.domain.Transaction;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class InMemoryTransactionRepositoryTest {

    private final InMemoryTransactionRepository repository = new InMemoryTransactionRepository();

    private static Transaction tx(long id, String type, Long parentId) {
        return new Transaction(id, BigDecimal.valueOf(100), type, parentId);
    }

    private static Collection<Long> childIds(InMemoryTransactionRepository repository, long parentId) {
        return repository.findChildren(parentId).stream().map(Transaction::transactionId).toList();
    }

    @Test
    void savesAndFindsById() {
        Transaction root = tx(1L, "x", null);
        repository.save(root);

        assertEquals(Optional.of(root), repository.findById(1L));
        assertEquals(Optional.empty(), repository.findById(99L));
    }

    @Test
    void indexesChildOnSave() {
        repository.save(tx(1L, "x", null));
        repository.save(tx(2L, "x", 1L));

        assertTrue(childIds(repository, 1L).contains(2L));
        assertTrue(repository.findChildren(2L).isEmpty());
    }

    @Test
    void reParentingMovesChildBetweenParents() {
        repository.save(tx(1L, "x", null));
        repository.save(tx(2L, "x", null));
        repository.save(tx(3L, "x", 1L));

        repository.save(tx(3L, "x", 2L));

        assertFalse(childIds(repository, 1L).contains(3L));
        assertTrue(childIds(repository, 2L).contains(3L));
    }

    @Test
    void promotingToRootRemovesFromOldParent() {
        repository.save(tx(1L, "x", null));
        repository.save(tx(2L, "x", 1L));

        repository.save(tx(2L, "x", null));

        assertFalse(childIds(repository, 1L).contains(2L));
    }

    @Test
    void findByTypeIsCaseSensitiveAndEmptyForUnknown() {
        repository.save(tx(10L, "cars", null));
        repository.save(tx(11L, "Cars", null));
        repository.save(tx(12L, "cars", null));

        assertEquals(2, repository.findByType("cars").size());
        assertTrue(repository.findByType("cars").containsAll(java.util.List.of(10L, 12L)));
        assertEquals(java.util.List.of(11L), repository.findByType("Cars").stream().toList());
        assertTrue(repository.findByType("boats").isEmpty());
    }

    @Test
    void findByTypeReflectsTypeChangeOnUpsert() {
        repository.save(tx(10L, "cars", null));

        repository.save(tx(10L, "shopping", null));

        assertTrue(repository.findByType("cars").isEmpty());
        assertEquals(java.util.List.of(10L), repository.findByType("shopping").stream().toList());
    }

    @Test
    void findChildrenIsEmptyAndNeverNullForLeaf() {
        repository.save(tx(1L, "x", null));

        Collection<Transaction> children = repository.findChildren(1L);

        assertTrue(children.isEmpty());
        for (Transaction child : children) {
            assertFalse(child == null);
        }
    }

    @Test
    void findAllContainsEverySavedTransaction() {
        repository.save(tx(1L, "x", null));
        repository.save(tx(2L, "x", 1L));

        assertEquals(2, repository.findAll().size());
        assertEquals(tx(2L, "x", 1L), repository.findAll().get(2L));
    }
}
