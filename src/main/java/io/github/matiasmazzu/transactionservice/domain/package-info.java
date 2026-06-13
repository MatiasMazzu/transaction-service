/**
 * Pure domain: immutable entities (e.g. {@code Transaction}) and pure validation
 * logic (e.g. {@code CycleChecker}).
 *
 * <p>Dependency rule: the domain is the center of the hexagonal architecture and
 * <strong>depends on nothing</strong>. No Spring annotations, no concurrency
 * primitives, no references to the port or the adapters.
 */
package io.github.matiasmazzu.transactionservice.domain;
