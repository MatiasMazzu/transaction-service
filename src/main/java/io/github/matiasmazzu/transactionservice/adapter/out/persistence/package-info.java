/**
 * Outbound adapter: in-memory implementation of the port
 * ({@code InMemoryTransactionRepository}) backed by concurrent data structures
 * ({@code ConcurrentHashMap} for the store and for the children index).
 *
 * <p>Dependency rule: depends on {@code application.port} (implements the
 * {@code TransactionRepository} interface) and on {@code domain} (uses the {@code Transaction} type).
 */
package io.github.matiasmazzu.transactionservice.adapter.out.persistence;
