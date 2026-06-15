package io.github.matiasmazzu.transactionservice.adapter.in.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.matiasmazzu.transactionservice.application.TransactionService;
import io.github.matiasmazzu.transactionservice.domain.Transaction;
import io.github.matiasmazzu.transactionservice.domain.exception.CycleDetectedException;
import io.github.matiasmazzu.transactionservice.domain.exception.ParentNotFoundException;
import io.github.matiasmazzu.transactionservice.domain.exception.TransactionNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionService service;

    @Test
    void putCreatesTransactionAndReturnsOk() throws Exception {
        mockMvc.perform(put("/transactions/{id}", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":5000.0,\"type\":\"cars\"}"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"status\":\"ok\"}"));

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(service).upsert(captor.capture());
        Transaction captured = captor.getValue();
        assertEquals(10L, captured.transactionId());
        assertEquals("cars", captured.type());
        assertEquals(0, captured.amount().compareTo(BigDecimal.valueOf(5000.0)));
        assertNull(captured.parentId());
    }

    @Test
    void putUpdatesExistingTransactionIdempotentlyAndReturnsOk() throws Exception {
        String body = "{\"amount\":9999.0,\"type\":\"shopping\",\"parent_id\":10}";

        for (int i = 0; i < 2; i++) {
            mockMvc.perform(put("/transactions/{id}", 11L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(content().json("{\"status\":\"ok\"}"));
        }

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(service, times(2)).upsert(captor.capture());
        List<Transaction> captured = captor.getAllValues();
        assertEquals(captured.get(0), captured.get(1));
        assertEquals(11L, captured.get(0).transactionId());
        assertEquals(10L, captured.get(0).parentId());
    }

    @Test
    void putAcceptsNegativeAmount() throws Exception {
        mockMvc.perform(put("/transactions/{id}", 12L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":-2500.0,\"type\":\"refund\"}"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"status\":\"ok\"}"));

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(service).upsert(captor.capture());
        assertEquals(0, captor.getValue().amount().compareTo(BigDecimal.valueOf(-2500.0)));
    }

    @Test
    void putWithMissingTypeReturnsBadRequest() throws Exception {
        mockMvc.perform(put("/transactions/{id}", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":5000.0}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void putWithBlankTypeReturnsBadRequest() throws Exception {
        mockMvc.perform(put("/transactions/{id}", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":5000.0,\"type\":\"   \"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void putWithMissingAmountReturnsBadRequest() throws Exception {
        mockMvc.perform(put("/transactions/{id}", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"cars\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void getTypesReturnsIdsForMatchingType() throws Exception {
        when(service.findByType("cars")).thenReturn(List.of(10L));

        mockMvc.perform(get("/transactions/types/{type}", "cars"))
                .andExpect(status().isOk())
                .andExpect(content().json("[10]"));
    }

    @Test
    void getTypesReturnsEmptyArrayWhenNoResults() throws Exception {
        when(service.findByType("ghost")).thenReturn(List.of());

        mockMvc.perform(get("/transactions/types/{type}", "ghost"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void getSumReturnsTransitiveSum() throws Exception {
        when(service.sum(10L)).thenReturn(BigDecimal.valueOf(20000));

        mockMvc.perform(get("/transactions/sum/{id}", 10L))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"sum\":20000.0}"));
    }

    @Test
    void getSumReturnsNotFoundProblemDetailForUnknownId() throws Exception {
        when(service.sum(99L)).thenThrow(new TransactionNotFoundException("transaction 99 not found"));

        mockMvc.perform(get("/transactions/sum/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.detail").value("transaction 99 not found"));
    }

    @Test
    void putWithNonexistentParentReturnsUnprocessableEntityProblemDetail() throws Exception {
        doThrow(new ParentNotFoundException("parent 99 not found for transaction 11"))
                .when(service).upsert(any());

        mockMvc.perform(put("/transactions/{id}", 11L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":5000.0,\"type\":\"shopping\",\"parent_id\":99}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.detail").value("parent 99 not found for transaction 11"));
    }

    @Test
    void putWithCycleReturnsUnprocessableEntityProblemDetail() throws Exception {
        doThrow(new CycleDetectedException("cycle detected assigning parent 11 to transaction 10"))
                .when(service).upsert(any());

        mockMvc.perform(put("/transactions/{id}", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":5000.0,\"type\":\"cars\",\"parent_id\":11}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.detail").value("cycle detected assigning parent 11 to transaction 10"));
    }

    @Test
    void putWithInvalidBodyReturnsBadRequestProblemDetail() throws Exception {
        mockMvc.perform(put("/transactions/{id}", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":5000.0}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Bad Request"));

        verifyNoInteractions(service);
    }
}
