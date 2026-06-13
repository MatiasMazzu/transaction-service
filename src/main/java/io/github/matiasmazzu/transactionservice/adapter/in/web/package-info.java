/**
 * Inbound REST adapter: controller ({@code TransactionController}) and centralized
 * error handling ({@code GlobalExceptionHandler} with {@code @RestControllerAdvice}, Problem
 * Details RFC 9457).
 *
 * <p>Dependency rule: depends on {@code application}. It <strong>never</strong> accesses
 * {@code adapter.out.persistence} directly.
 */
package io.github.matiasmazzu.transactionservice.adapter.in.web;
