# ADR-0007: Traversal iterativo en el cálculo de la suma

- **Estado:** Aceptada
- **Fecha:** 2026-06-15

## Contexto

La suma transitiva recorre el subárbol de descendientes de una transacción. Una cadena padre-hijo muy profunda podría desbordar la pila de llamadas.

## Decisión

`TransactionService.sum` recorre el subárbol con una **pila explícita** (`ArrayDeque`) y un `Set` de visitados, acumulando en `BigDecimal`. Sin recursión.

## Alternativa descartada

Recursión sobre los hijos: más concisa, pero arriesga `StackOverflowError` en cadenas profundas y ata la profundidad máxima al tamaño de la pila de la JVM.

## Consecuencia

O(tamaño del subárbol) en tiempo, profundidad limitada solo por memoria de heap. El `set` de visitados protege ante datos transitorios inconsistentes ([ADR-0005](0005-lecturas-lock-free.md)) evitando doble conteo o bucles.
