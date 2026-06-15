package io.github.matiasmazzu.transactionservice.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import io.github.matiasmazzu.transactionservice.adapter.out.persistence.InMemoryTransactionRepository;
import io.github.matiasmazzu.transactionservice.domain.CycleChecker;
import io.github.matiasmazzu.transactionservice.domain.Transaction;
import io.github.matiasmazzu.transactionservice.domain.exception.CycleDetectedException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

class TransactionServiceConcurrencyTest {

    private static final int PAIR_COUNT = 40;
    private static final int RING_SIZE = 20;
    private static final long RING_ID_BASE = 10_000L;

    private final InMemoryTransactionRepository repository = new InMemoryTransactionRepository();
    private final TransactionService service = new TransactionService(repository, new CycleChecker());

    private static BigDecimal amountOf(long id) {
        return BigDecimal.valueOf(id);
    }

    private static long ringId(int index) {
        return RING_ID_BASE + index;
    }

    @Test
    @Timeout(30)
    @DisplayName("Concurrent cycle-closing writes keep the graph acyclic and sums consistent "
            + "(probabilistic evidence: this scenario closes cycles without the lock, stays clean with it)")
    void concurrentCycleAttemptsLeaveGraphAcyclicAndSumsConsistent() throws Exception {
        List<Transaction> concurrentUpserts = seedRootsAndBuildConcurrentUpserts();

        int parties = concurrentUpserts.size();
        CyclicBarrier barrier = new CyclicBarrier(parties);
        ConcurrentLinkedQueue<Throwable> unexpected = new ConcurrentLinkedQueue<>();
        AtomicInteger rejectedByLock = new AtomicInteger();

        List<Callable<Void>> workers = new ArrayList<>(parties);
        for (Transaction target : concurrentUpserts) {
            workers.add(() -> {
                try {
                    barrier.await();
                    service.upsert(target);
                } catch (CycleDetectedException rejected) {
                    rejectedByLock.incrementAndGet();
                } catch (Throwable other) {
                    unexpected.add(other);
                }
                return null;
            });
        }

        ExecutorService pool = Executors.newFixedThreadPool(parties);
        try {
            for (Future<Void> future : pool.invokeAll(workers)) {
                future.get();
            }
        } finally {
            pool.shutdownNow();
        }

        if (!unexpected.isEmpty()) {
            fail("Unexpected exceptions under concurrency: " + unexpected);
        }

        Map<Long, Transaction> graph = repository.findAll();
        assertGraphIsAcyclic(graph);
        assertNoOrphans(graph);
        assertSomeParentEdgesAccepted(graph);
        assertForestSumInvariant(graph);
        assertTrue(rejectedByLock.get() >= PAIR_COUNT,
                "Expected the lock to reject at least one cycle-closing write per pair under contention");
    }

    private List<Transaction> seedRootsAndBuildConcurrentUpserts() {
        List<Transaction> concurrentUpserts = new ArrayList<>();

        for (int i = 0; i < PAIR_COUNT; i++) {
            long a = 2L * i + 1;
            long b = 2L * i + 2;
            service.upsert(new Transaction(a, amountOf(a), "pair", null));
            service.upsert(new Transaction(b, amountOf(b), "pair", null));
            concurrentUpserts.add(new Transaction(a, amountOf(a), "pair", b));
            concurrentUpserts.add(new Transaction(b, amountOf(b), "pair", a));
        }

        for (int j = 0; j < RING_SIZE; j++) {
            long id = ringId(j);
            service.upsert(new Transaction(id, amountOf(id), "ring", null));
        }
        for (int j = 0; j < RING_SIZE; j++) {
            long id = ringId(j);
            long next = ringId((j + 1) % RING_SIZE);
            concurrentUpserts.add(new Transaction(id, amountOf(id), "ring", next));
        }

        return concurrentUpserts;
    }

    private static void assertGraphIsAcyclic(Map<Long, Transaction> graph) {
        for (Long startId : graph.keySet()) {
            Set<Long> visited = new HashSet<>();
            Long current = startId;
            while (current != null) {
                if (!visited.add(current)) {
                    fail("Cycle detected in ancestor chain starting at " + startId);
                }
                Transaction node = graph.get(current);
                if (node == null) {
                    break;
                }
                current = node.parentId();
            }
        }
    }

    private static void assertSomeParentEdgesAccepted(Map<Long, Transaction> graph) {
        int roots = 0;
        for (Transaction transaction : graph.values()) {
            if (transaction.parentId() == null) {
                roots++;
            }
        }
        assertTrue(roots < graph.size(),
                "Expected at least one parent edge to be accepted under contention");
    }

    private static void assertNoOrphans(Map<Long, Transaction> graph) {
        for (Transaction transaction : graph.values()) {
            Long parentId = transaction.parentId();
            if (parentId != null) {
                assertTrue(graph.containsKey(parentId),
                        "Orphan transaction " + transaction.transactionId() + " references missing parent " + parentId);
            }
        }
    }

    private void assertForestSumInvariant(Map<Long, Transaction> graph) {
        BigDecimal total = BigDecimal.ZERO;
        for (Transaction transaction : graph.values()) {
            total = total.add(transaction.amount());
        }

        BigDecimal sumViaRoots = BigDecimal.ZERO;
        for (Transaction transaction : graph.values()) {
            if (transaction.parentId() == null) {
                sumViaRoots = sumViaRoots.add(service.sum(transaction.transactionId()));
            }
        }

        assertEquals(0, sumViaRoots.compareTo(total),
                "Sum over roots (" + sumViaRoots + ") must equal total amount (" + total + ")");
    }
}
