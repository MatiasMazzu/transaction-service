package io.github.matiasmazzu.transactionservice;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.client.RestTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TransactionFlowIT {

    @LocalServerPort
    private int port;

    private RestTestClient client;

    record SumView(double sum) {
    }

    @BeforeEach
    void setUp() {
        client = RestTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
    }

    @Test
    void reproducesStatementSequence() {
        put(10L, "{\"amount\":5000.0,\"type\":\"cars\"}");
        put(11L, "{\"amount\":10000.0,\"type\":\"shopping\",\"parent_id\":10}");
        put(12L, "{\"amount\":5000.0,\"type\":\"shopping\",\"parent_id\":11}");

        SumView sum10 = client.get().uri("/transactions/sum/10").exchange()
                .expectStatus().isOk()
                .expectBody(SumView.class).returnResult().getResponseBody();
        assertEquals(20000.0, sum10.sum());

        SumView sum11 = client.get().uri("/transactions/sum/11").exchange()
                .expectStatus().isOk()
                .expectBody(SumView.class).returnResult().getResponseBody();
        assertEquals(15000.0, sum11.sum());

        Long[] carsIds = client.get().uri("/transactions/types/cars").exchange()
                .expectStatus().isOk()
                .expectBody(Long[].class).returnResult().getResponseBody();
        assertArrayEquals(new Long[] {10L}, carsIds);
    }

    @Test
    void chainingToNonexistentParentReturns422ProblemDetail() {
        client.put().uri("/transactions/99")
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"amount\":1000.0,\"type\":\"orphan\",\"parent_id\":999}")
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.status").isEqualTo(422)
                .jsonPath("$.detail").value(String.class, detail -> assertTrue(detail.contains("999")));
    }

    @Test
    void closingACycleReturns422ProblemDetail() {
        put(10L, "{\"amount\":5000.0,\"type\":\"cars\"}");
        put(11L, "{\"amount\":10000.0,\"type\":\"shopping\",\"parent_id\":10}");

        client.put().uri("/transactions/10")
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"amount\":5000.0,\"type\":\"cars\",\"parent_id\":11}")
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.status").isEqualTo(422)
                .jsonPath("$.detail").value(String.class, detail -> assertTrue(detail.contains("cycle")));
    }

    private void put(long transactionId, String body) {
        client.put().uri("/transactions/" + transactionId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .exchange()
                .expectStatus().isOk()
                .expectBody().json("{\"status\":\"ok\"}");
    }
}
