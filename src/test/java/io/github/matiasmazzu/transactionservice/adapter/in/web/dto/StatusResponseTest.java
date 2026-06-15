package io.github.matiasmazzu.transactionservice.adapter.in.web.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class StatusResponseTest {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Test
    void okBuildsStatusOk() {
        assertEquals("ok", StatusResponse.ok().status());
    }

    @Test
    void serializesToStatusOkWithoutWrapper() {
        String json = jsonMapper.writeValueAsString(StatusResponse.ok());

        assertEquals("{\"status\":\"ok\"}", json);
    }
}
