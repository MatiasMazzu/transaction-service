package io.github.matiasmazzu.transactionservice.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class TransactionTest {

    @Test
    void exposesGivenFields() {
        Transaction tx = new Transaction(10L, new BigDecimal("5000"), "cars", null);

        assertEquals(10L, tx.transactionId());
        assertEquals(new BigDecimal("5000"), tx.amount());
        assertEquals("cars", tx.type());
        assertEquals(null, tx.parentId());
    }

    @Test
    void rootTransactionHasNoParent() {
        Transaction root = new Transaction(10L, new BigDecimal("5000"), "cars", null);

        assertTrue(root.isRoot());
        assertEquals(null, root.parentId());
    }

    @Test
    void childTransactionReferencesItsParent() {
        Transaction child = new Transaction(11L, new BigDecimal("10000"), "shopping", 10L);

        assertFalse(child.isRoot());
        assertEquals(10L, child.parentId());
    }

    @Test
    void negativeAmountAcceptedAndPreservedExactly() {
        BigDecimal negative = new BigDecimal("-150.75");
        Transaction tx = new Transaction(1L, negative, "refund", null);

        assertEquals(0, tx.amount().compareTo(negative), "same numeric value");
        assertEquals(negative, tx.amount(), "same value and scale (no rounding, no scale change)");
        assertEquals(2, tx.amount().scale(), "original scale is preserved");
    }

    @Test
    void amountScaleIsNotNormalized() {
        Transaction tx = new Transaction(1L, new BigDecimal("2.00"), "x", null);

        assertEquals(new BigDecimal("2.00"), tx.amount());
        assertEquals(2, tx.amount().scale());
    }

    @Test
    void zeroAmountPreservedExactly() {
        BigDecimal zero = new BigDecimal("0.00");
        Transaction tx = new Transaction(1L, zero, "adjustment", null);

        assertEquals(zero, tx.amount());
        assertEquals(2, tx.amount().scale());
    }

    @Test
    void equalByValueWhenAllFieldsMatch() {
        Transaction a = new Transaction(10L, new BigDecimal("5000"), "cars", 7L);
        Transaction b = new Transaction(10L, new BigDecimal("5000"), "cars", 7L);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void notEqualWhenAnyFieldDiffers() {
        Transaction base = new Transaction(10L, new BigDecimal("5000"), "cars", 7L);

        assertNotEquals(base, new Transaction(99L, new BigDecimal("5000"), "cars", 7L));
        assertNotEquals(base, new Transaction(10L, new BigDecimal("5001"), "cars", 7L));
        assertNotEquals(base, new Transaction(10L, new BigDecimal("5000"), "books", 7L));
        assertNotEquals(base, new Transaction(10L, new BigDecimal("5000"), "cars", null));
    }

    @Test
    void notEqualWhenAmountScaleDiffers() {
        Transaction a = new Transaction(10L, new BigDecimal("5000"), "cars", null);
        Transaction b = new Transaction(10L, new BigDecimal("5000.00"), "cars", null);

        assertEquals(0, a.amount().compareTo(b.amount()), "same numeric value");
        assertNotEquals(a, b, "different scale ⇒ not equal");
        assertNotEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void amountReferenceIsStable() {
        BigDecimal amount = new BigDecimal("5000");
        Transaction tx = new Transaction(10L, amount, "cars", null);

        assertSame(amount, tx.amount(), "amount is stored as-is, without copy/transformation");
    }

    @Test
    void requiredFieldsRejectNull() {
        assertThrows(NullPointerException.class,
                () -> new Transaction(null, new BigDecimal("1"), "cars", null));
        assertThrows(NullPointerException.class,
                () -> new Transaction(10L, null, "cars", null));
        assertThrows(NullPointerException.class,
                () -> new Transaction(10L, new BigDecimal("1"), null, null));
    }
}
