# ADR-0001: Almacenamiento in-memory

- **Estado:** Aceptada
- **Fecha:** 2026-06-15

## Contexto

El servicio debe registrar transacciones y calcular sumas transitivas sobre un grafo padre-hijo. El alcance no exige persistencia durable ni consultas analíticas.

## Decisión

Almacenar el estado en memoria: un `ConcurrentHashMap<Long, Transaction>` como store y un índice de hijos `ConcurrentHashMap<Long, Set<Long>>`, encapsulados en `InMemoryTransactionRepository` detrás del puerto `TransactionRepository`.

## Alternativa descartada

Base de datos real (Postgres/H2 + JPA): aporta durabilidad pero suma esquema, migraciones, mapeo ORM e infra de test (Testcontainers) que el alcance no justifica.

## Consecuencia

Arranque simple y tests rápidos. **Sin durabilidad**: el estado se pierde al reiniciar (límite aceptado). La concurrencia se resuelve en memoria (ver [ADR-0004](0004-reentrantlock-en-application.md)). El puerto permite sustituir el adapter por uno persistente sin tocar dominio ni aplicación.
