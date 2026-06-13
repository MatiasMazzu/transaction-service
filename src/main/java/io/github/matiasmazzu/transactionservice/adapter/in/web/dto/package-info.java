/**
 * Request/response DTOs for the REST boundary (e.g. {@code TransactionRequest},
 * {@code SumResponse}, {@code StatusResponse}).
 *
 * <p>Conversion boundary {@code double} ↔ {@code BigDecimal} ({@code BigDecimal.valueOf}
 * on the way in, {@code doubleValue()} on the way out). The domain entity {@code Transaction}
 * is never exposed here.
 */
package io.github.matiasmazzu.transactionservice.adapter.in.web.dto;
