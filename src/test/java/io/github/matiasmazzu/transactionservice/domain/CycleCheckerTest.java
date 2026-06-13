package io.github.matiasmazzu.transactionservice.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.matiasmazzu.transactionservice.domain.exception.CycleDetectedException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

class CycleCheckerTest {

    private final CycleChecker cycleChecker = new CycleChecker();

    private static Map<Long, Transaction> graphOf(Transaction... nodes) {
        Map<Long, Transaction> graph = new HashMap<>();
        for (Transaction node : nodes) {
            graph.put(node.transactionId(), node);
        }
        return graph;
    }

    private static Transaction node(long id, Long parentId) {
        return new Transaction(id, BigDecimal.ZERO, "x", parentId);
    }

    @Test
    void selfReferenceIsDetected() {
        Map<Long, Transaction> graph = graphOf(node(1L, null));

        assertTrue(cycleChecker.wouldFormCycle(graph, 1L, 1L));
    }

    @Test
    void cycleDetectedExceptionIsAvailable() {
        RuntimeException ex = new CycleDetectedException("cycle: 1 -> 1");

        assertEquals("cycle: 1 -> 1", ex.getMessage());
    }

    @Test
    void detectsCycleWhenTransactionIsAncestorOfProposedParent() {
        Map<Long, Transaction> graph = graphOf(node(1L, null), node(2L, 1L), node(3L, 2L));

        assertTrue(cycleChecker.wouldFormCycle(graph, 1L, 3L));
    }

    @Test
    void allowsValidReParenting() {
        Map<Long, Transaction> graph = graphOf(node(1L, null), node(2L, 1L), node(3L, 2L));

        assertFalse(cycleChecker.wouldFormCycle(graph, 3L, 1L));
    }

    @Test
    void allowsReParentingAcrossIndependentBranches() {
        Map<Long, Transaction> graph =
                graphOf(node(1L, null), node(2L, 1L), node(3L, null), node(4L, 3L));

        assertFalse(cycleChecker.wouldFormCycle(graph, 2L, 4L));
    }

    @Test
    void rootProposalNeverFormsCycle() {
        Map<Long, Transaction> graph = graphOf(node(1L, null), node(2L, 1L));

        assertFalse(cycleChecker.wouldFormCycle(graph, 5L, null));
    }

    @Test
    void allowsNewNodeWithExistingParent() {
        Map<Long, Transaction> graph = graphOf(node(1L, null));

        assertFalse(cycleChecker.wouldFormCycle(graph, 99L, 1L));
    }

    @Test
    void missingProposedParentIsNotACycle() {
        Map<Long, Transaction> graph = graphOf(node(1L, null), node(2L, 1L));

        assertFalse(cycleChecker.wouldFormCycle(graph, 2L, 777L));
    }

    @Test
    @Timeout(10)
    void handlesDeepChainWithoutStackOverflow() {
        int depth = 100_000;
        Map<Long, Transaction> graph = new HashMap<>(depth * 2);
        graph.put(1L, node(1L, null));
        for (long id = 2; id <= depth; id++) {
            graph.put(id, node(id, id - 1));
        }

        assertTrue(cycleChecker.wouldFormCycle(graph, 1L, (long) depth));
        assertFalse(cycleChecker.wouldFormCycle(graph, 999_999_999L, (long) depth));
    }

    @Test
    @Timeout(10)
    void terminatesEvenIfInputGraphAlreadyContainsACycle() {
        Map<Long, Transaction> graph = graphOf(node(1L, 2L), node(2L, 1L));

        assertFalse(cycleChecker.wouldFormCycle(graph, 99L, 1L));
    }
}
