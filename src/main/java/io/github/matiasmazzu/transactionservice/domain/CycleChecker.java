package io.github.matiasmazzu.transactionservice.domain;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CycleChecker {

    public boolean wouldFormCycle(Map<Long, Transaction> graph, long transactionId, Long proposedParentId) {
        if (proposedParentId == null) {
            return false;
        }
        if (proposedParentId == transactionId) {
            return true;
        }
        Set<Long> visited = new HashSet<>();
        Long current = proposedParentId;
        while (current != null) {
            if (current == transactionId) {
                return true;
            }
            if (!visited.add(current)) {
                break;
            }
            Transaction node = graph.get(current);
            if (node == null) {
                break;
            }
            current = node.parentId();
        }
        return false;
    }
}
