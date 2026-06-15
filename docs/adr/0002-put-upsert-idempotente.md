# ADR-0002: `PUT` upsert idempotente

- **Estado:** Aceptada
- **Fecha:** 2026-06-15

## Contexto

El alta y la actualización de una transacción comparten la semántica de "estado deseado" para un id conocido (provisto en la URL).

## Decisión

Un único `PUT /transactions/{id}` que hace upsert (alta o actualización) y responde siempre `200 {"status":"ok"}`.

## Alternativa descartada

`POST` para alta + `PUT` para actualización, con `201`/`404` según el recurso exista: más superficie de API y semántica no idempotente, sin beneficio para este contrato.

## Consecuencia

Reintentos seguros (idempotencia). El cliente no necesita distinguir alta de actualización. El contrato de respuesta es uniforme.
