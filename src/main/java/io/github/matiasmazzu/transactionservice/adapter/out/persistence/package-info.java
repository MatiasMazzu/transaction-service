/**
 * Adapter de salida: implementación in-memory del puerto
 * ({@code InMemoryTransactionRepository}) con estructuras concurrentes
 * ({@code ConcurrentHashMap} para el store y para el índice de hijos).
 *
 * <p>Regla de dependencias: depende de {@code application.port} (implementa la interfaz
 * {@code TransactionRepository}) y de {@code domain} (usa el tipo {@code Transaction}).
 */
package io.github.matiasmazzu.transactionservice.adapter.out.persistence;
