package io.github.matiasmazzu.transactionservice.adapter.in.web.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.matiasmazzu.transactionservice.domain.Transaction;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class TransactionRequestTest {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Test
    void toTransactionConvertsAmountWithValueOfNotDoubleConstructor() {
        TransactionRequest request = new TransactionRequest(0.1, "cars", 10L);

        Transaction transaction = request.toTransaction(7L);

        assertEquals(7L, transaction.transactionId());
        assertEquals("cars", transaction.type());
        assertEquals(10L, transaction.parentId());
        assertEquals(new BigDecimal("0.1"), transaction.amount());
        assertNotEquals(new BigDecimal(0.1), transaction.amount());
    }

    @Test
    void toTransactionPreservesNegativeAmountAndRoot() {
        TransactionRequest request = new TransactionRequest(-2500.0, "refund", null);

        Transaction transaction = request.toTransaction(3L);

        assertEquals(0, transaction.amount().compareTo(BigDecimal.valueOf(-2500.0)));
        assertTrue(transaction.isRoot());
    }

    @Test
    void deserializesParentIdFromSnakeCase() {
        String json = "{\"amount\":100.0,\"type\":\"cars\",\"parent_id\":10}";

        TransactionRequest request = jsonMapper.readValue(json, TransactionRequest.class);

        assertEquals(10L, request.parentId());
        assertEquals("cars", request.type());
        assertEquals(100.0, request.amount(), 0.0);
    }

    @Test
    void deserializesRootWhenParentIdAbsent() {
        String json = "{\"amount\":100.0,\"type\":\"cars\"}";

        TransactionRequest request = jsonMapper.readValue(json, TransactionRequest.class);

        assertNull(request.parentId());
    }

    @Test
    void serializesToLiteralContractWithParentIdSnakeCase() {
        TransactionRequest request = new TransactionRequest(100.0, "cars", 10L);

        String json = jsonMapper.writeValueAsString(request);

        assertEquals("{\"amount\":100.0,\"type\":\"cars\",\"parent_id\":10}", json);
    }
}
