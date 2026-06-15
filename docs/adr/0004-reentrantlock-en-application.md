# ADR-0004: `ReentrantLock` en la capa de aplicación

- **Estado:** Aceptada
- **Fecha:** 2026-06-15

## Contexto

Las invariantes del grafo (el `parent` existe; no se forman ciclos) deben **validarse y aplicarse atómicamente**: validar fuera de la sección crítica reabre una carrera entre la validación y la escritura.

## Decisión

Un `ReentrantLock` alojado en `TransactionService` envuelve, en `upsert`, la validación (cycle check + parent check) y la mutación (`save`) en una sola sección crítica. Ningún adapter (controller o repository) toma el lock.

## Alternativa descartada

- `synchronized`: más rígido (sin `tryLock`/timeouts/equidad si se necesitaran) y atado al monitor del objeto.
- `ReadWriteLock`: bloquearía las lecturas durante la escritura, contradiciendo las lecturas no bloqueantes ([ADR-0005](0005-lecturas-lock-free.md)).
- Lock en el repository: filtra la responsabilidad de concurrencia al adapter y no protege la invariante **compuesta** (validar+mutar), que es de la capa de aplicación.

## Consecuencia

Las escrituras concurrentes no pueden violar las invariantes ni dejar el grafo a medio actualizar. Las escrituras se serializan (aceptable para el alcance in-memory). La sección crítica queda acotada a la orquestación.
