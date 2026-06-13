package io.github.matiasmazzu.transactionservice.domain;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Immutable domain entity for a transaction in the parent-child graph.
 *
 * <p>The {@code amount} is kept exactly as provided: no rounding and no scale change.
 * Negative amounts (refunds/adjustments) are valid. A {@code null parentId} means the
 * transaction is a root. Equality is structural (value and scale of {@code amount}).
 */
public record Transaction(Long transactionId, BigDecimal amount, String type, Long parentId) {

    public Transaction {
        Objects.requireNonNull(transactionId, "transactionId");
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(type, "type");
        // parentId may be null (root); negative amount is allowed; scale is not normalized.
    }

    /** Returns {@code true} when the transaction has no parent (graph root). */
    public boolean isRoot() {
        return parentId == null;
    }
}
