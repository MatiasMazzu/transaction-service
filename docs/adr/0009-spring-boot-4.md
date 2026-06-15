# ADR-0009: Spring Boot 4.0.x sobre 3.x

- **Estado:** Aceptada
- **Fecha:** 2026-06-15

## Contexto

Al iniciar el proyecto había que elegir la línea de Spring Boot. La 3.x estaba más madura; la 4.0.x era reciente.

## Decisión

Usar **Spring Boot 4.0.7** sobre Java 21.

## Alternativa descartada

Spring Boot 3.x: más rodaje y ejemplos al momento, pero con un horizonte de soporte más corto (más cerca de EOL).

## Consecuencia

Mayor horizonte de soporte a futuro. A cambio, se absorben las novedades de la 4.x, documentadas en el camino:

- **Jackson 3** (`tools.jackson.*`, `JsonMapper` inmutable); `@JsonProperty` sigue en `com.fasterxml.jackson.annotation`.
- Starters de test **modulares**: `@WebMvcTest` reubicado (`org.springframework.boot.webmvc.test.autoconfigure`), `@MockBean` removido (usar `@MockitoBean`).
- `TestRestTemplate` movido al módulo `spring-boot-resttestclient`; para los e2e `@SpringBootTest(RANDOM_PORT)` se usa `RestTestClient` (de Spring Framework), que evita una dependencia extra.
