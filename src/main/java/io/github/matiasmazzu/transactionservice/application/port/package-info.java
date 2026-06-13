/**
 * Ports (interfaces) consumed by the application layer, e.g.
 * {@code TransactionRepository}.
 *
 * <p>They live here —and not in {@code domain}— because a port belongs with its consumer
 * ({@code application}); the domain stays pure and never uses the repository.
 */
package io.github.matiasmazzu.transactionservice.application.port;
