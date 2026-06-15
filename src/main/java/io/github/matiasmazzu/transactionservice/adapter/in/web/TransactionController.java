package io.github.matiasmazzu.transactionservice.adapter.in.web;

import io.github.matiasmazzu.transactionservice.adapter.in.web.dto.StatusResponse;
import io.github.matiasmazzu.transactionservice.adapter.in.web.dto.TransactionRequest;
import io.github.matiasmazzu.transactionservice.application.TransactionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    @PutMapping("/{transactionId}")
    public StatusResponse upsert(@PathVariable long transactionId, @Valid @RequestBody TransactionRequest request) {
        service.upsert(request.toTransaction(transactionId));
        return StatusResponse.ok();
    }
}
