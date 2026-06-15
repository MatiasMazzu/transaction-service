# ADR-0005: Lecturas lock-free (weakly consistent)

- **Estado:** Aceptada
- **Fecha:** 2026-06-15

## Contexto

Tomar el lock de escritura en cada lectura penalizaría el caso común (consultas de suma y por tipo) y serializaría lecturas contra escrituras.

## Decisión

`sum` y `findByType` leen **sin** tomar el lock, directamente sobre las estructuras concurrentes (`ConcurrentHashMap` + índice de hijos). El `ConcurrentHashMap` garantiza iteración segura bajo escritura concurrente.

## Alternativa descartada

Lecturas bajo el mismo lock que las escrituras: dan consistencia fuerte (snapshot), pero bloquean y contradicen el requisito de lecturas no bloqueantes.

## Consecuencia

Lecturas no bloqueantes. Bajo escritura concurrente, una lectura puede observar un estado **transitorio** (weakly consistent) — p. ej. un hijo recién re-parentado visto en transición. Es un límite **aceptado y consciente**, coherente con el alcance in-memory, no un defecto. El `set` de visitados en el traversal evita doble conteo dentro de una misma lectura.
