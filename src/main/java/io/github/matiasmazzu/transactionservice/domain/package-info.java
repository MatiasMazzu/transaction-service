/**
 * Dominio puro: entidades inmutables (p. ej. {@code Transaction}) y lógica de validación
 * pura (p. ej. {@code CycleChecker}).
 *
 * <p>Regla de dependencias: el dominio es el centro de la arquitectura hexagonal y
 * <strong>no depende de nada</strong>. Sin anotaciones de Spring, sin primitivas de
 * concurrencia, sin referencias al puerto ni a los adapters.
 */
package io.github.matiasmazzu.transactionservice.domain;
