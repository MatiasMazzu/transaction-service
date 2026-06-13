/**
 * Domain exceptions representing business-rule violations of the transaction graph,
 * e.g. {@code CycleDetectedException}.
 *
 * <p>They are defined in the domain because the rules they protect are domain rules; they
 * are thrown by the application service and mapped to HTTP error responses by the web adapter.
 */
package io.github.matiasmazzu.transactionservice.domain.exception;
