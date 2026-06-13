/**
 * Servicios de aplicación que orquestan los casos de uso (p. ej. {@code TransactionService}).
 * Aquí se aloja el {@code ReentrantLock} que serializa la escritura (stories futuras),
 * envolviendo validación y mutación en una sola sección crítica.
 *
 * <p>Regla de dependencias: depende de {@code domain} y declara los puertos en
 * {@code application.port}. No conoce Spring Web ni la implementación in-memory.
 */
package io.github.matiasmazzu.transactionservice.application;
