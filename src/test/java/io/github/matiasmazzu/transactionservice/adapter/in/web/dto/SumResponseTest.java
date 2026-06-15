package io.github.matiasmazzu.transactionservice.adapter.in.web.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class SumResponseTest {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Test
    void ofConvertsBigDecimalToDouble() {
        SumResponse response = SumResponse.of(new BigDecimal("20000"));

        assertEquals(20000.0, response.sum(), 0.0);
    }

    @Test
    void serializesToSumNumberWithoutWrapper() {
        String json = jsonMapper.writeValueAsString(SumResponse.of(new BigDecimal("15000")));

        assertEquals("{\"sum\":15000.0}", json);
    }
}
