/**
 * DTOs de request/response del borde REST (p. ej. {@code TransactionRequest},
 * {@code SumResponse}, {@code StatusResponse}).
 *
 * <p>Frontera de conversión {@code double} ↔ {@code BigDecimal} ({@code BigDecimal.valueOf}
 * al entrar, {@code doubleValue()} al salir). La entidad de dominio {@code Transaction}
 * nunca se expone aquí.
 */
package io.github.matiasmazzu.transactionservice.adapter.in.web.dto;
