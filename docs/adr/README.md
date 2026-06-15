# Architecture Decision Records (ADRs)

Registro corto de las decisiones de diseño del servicio: cada una con contexto, decisión, alternativa descartada y consecuencia. El objetivo es explicar el **porqué**, no solo el qué.

| ADR | Decisión |
|-----|----------|
| [0001](0001-almacenamiento-in-memory.md) | Almacenamiento in-memory (sin base de datos) |
| [0002](0002-put-upsert-idempotente.md) | `PUT` upsert idempotente |
| [0003](0003-bigdecimal-en-dominio.md) | `BigDecimal` en el dominio, `double` solo en el borde |
| [0004](0004-reentrantlock-en-application.md) | `ReentrantLock` en la capa de aplicación |
| [0005](0005-lecturas-lock-free.md) | Lecturas lock-free (weakly consistent) |
| [0006](0006-scan-para-findbytype.md) | Scan para `findByType` (sin índice de tipos) |
| [0007](0007-traversal-iterativo.md) | Traversal iterativo en el cálculo de la suma |
| [0008](0008-errores-problem-details.md) | Errores centralizados (Problem Details, RFC 9457) |
| [0009](0009-spring-boot-4.md) | Spring Boot 4.0.x sobre 3.x |
