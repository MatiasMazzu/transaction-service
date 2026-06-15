package io.github.matiasmazzu.transactionservice.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.matiasmazzu.transactionservice.domain.Transaction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record TransactionRequest(
        @NotNull Double amount,
        @NotBlank String type,
        @JsonProperty("parent_id") Long parentId) {

    public Transaction toTransaction(long transactionId) {
        return new Transaction(transactionId, BigDecimal.valueOf(amount), type, parentId);
    }
}
