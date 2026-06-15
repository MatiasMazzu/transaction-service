# ADR-0003: `BigDecimal` en el dominio, `double` solo en el borde

- **Estado:** Aceptada
- **Fecha:** 2026-06-15

## Contexto

El dinero exige precisión exacta, pero el contrato HTTP del enunciado expresa `amount`/`sum` como `double`.

## Decisión

El dominio modela `amount` como `BigDecimal`. La conversión vive **solo** en los DTOs del adapter web: `BigDecimal.valueOf(amount)` al entrar y `doubleValue()` al salir. El dominio nunca ve `double`.

## Alternativa descartada

- `double` en el dominio: acumula errores de redondeo en las sumas.
- `new BigDecimal(double)`: arrastra el ruido binario del `double` (p. ej. `0.1` → `0.1000000000000000055…`); `BigDecimal.valueOf` usa la representación canónica.

## Consecuencia

Cálculos exactos en el dominio; el `double` queda confinado al borde HTTP. El render de salida puede mostrar decimal (`20000.0`), numéricamente equivalente al entero del enunciado.
