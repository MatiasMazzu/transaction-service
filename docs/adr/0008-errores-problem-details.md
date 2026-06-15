# ADR-0008: Errores centralizados (Problem Details, RFC 9457)

- **Estado:** Aceptada
- **Fecha:** 2026-06-15

## Contexto

El contrato de errores debe ser uniforme (`400`/`404`/`422`) y el dominio no debe conocer detalles de HTTP.

## Decisión

Un único `@RestControllerAdvice` (`GlobalExceptionHandler`, que extiende `ResponseEntityExceptionHandler`) mapea los errores a `application/problem+json` (RFC 9457): `400` body inválido (framework), `404` `TransactionNotFoundException`, `422` `ParentNotFoundException`/`CycleDetectedException`. Las excepciones de dominio son `RuntimeException` planas, sin anotaciones Spring.

## Alternativa descartada

- Mapear errores en el controller: duplica lógica y mezcla transporte con orquestación.
- `@ResponseStatus` sobre las excepciones de dominio: acopla el dominio a Spring/HTTP.

## Consecuencia

Un solo punto de verdad para el mapeo excepción→status. El dominio queda libre de acoplamiento al transporte. Extender `ResponseEntityExceptionHandler` hace que también los errores built-in de Spring MVC (validación, body malformado) salgan en `problem+json` de forma consistente.
