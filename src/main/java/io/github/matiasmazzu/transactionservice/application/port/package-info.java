/**
 * Puertos (interfaces) consumidos por la capa de aplicación, p. ej.
 * {@code TransactionRepository}.
 *
 * <p>Viven aquí —y no en {@code domain}— porque el puerto va donde está su consumidor
 * ({@code application}); el dominio permanece puro y nunca usa el repositorio.
 */
package io.github.matiasmazzu.transactionservice.application.port;
