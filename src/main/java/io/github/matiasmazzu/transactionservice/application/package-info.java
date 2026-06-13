/**
 * Application services that orchestrate the use cases (e.g. {@code TransactionService}).
 * This is where the {@code ReentrantLock} that serializes writes lives, wrapping
 * validation and mutation in a single critical section.
 *
 * <p>Dependency rule: depends on {@code domain} and declares the ports in
 * {@code application.port}. It knows nothing about Spring Web or the in-memory implementation.
 */
package io.github.matiasmazzu.transactionservice.application;
