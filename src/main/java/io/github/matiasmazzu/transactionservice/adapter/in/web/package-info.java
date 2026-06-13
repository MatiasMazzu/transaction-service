/**
 * Adapter de entrada REST: controller ({@code TransactionController}) y manejo centralizado
 * de errores ({@code GlobalExceptionHandler} con {@code @RestControllerAdvice}, Problem
 * Details RFC 9457).
 *
 * <p>Regla de dependencias: depende de {@code application}. <strong>Nunca</strong> accede
 * directamente a {@code adapter.out.persistence}.
 */
package io.github.matiasmazzu.transactionservice.adapter.in.web;
