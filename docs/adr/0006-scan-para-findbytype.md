# ADR-0006: Scan para `findByType` (sin índice de tipos)

- **Estado:** Aceptada
- **Fecha:** 2026-06-15

## Contexto

El traversal de la suma recorre repetidamente los hijos de cada nodo, por lo que se mantiene un índice materializado `childIdsByParent`. La consulta por tipo (`GET /transactions/types/{type}`) plantea si conviene un índice análogo por `type`.

## Decisión

`findByType` resuelve con un **scan O(n)** sobre `store.values()`. **No** se mantiene un índice por tipo.

## Alternativa descartada

Índice `Map<String, Set<Long>>` por tipo: ahorraría el scan, pero agrega estado a mantener en cada `save`/re-tipado, con riesgo de inconsistencia con el store, sin beneficio real para el volumen del alcance (YAGNI).

## Consecuencia

`findByType` es O(n) por consulta (aceptable in-memory con datasets chicos). Menos estado mutable que mantener sincronizado. El índice de hijos **sí** se justifica porque el traversal lo consulta repetidamente; el de tipos no.
