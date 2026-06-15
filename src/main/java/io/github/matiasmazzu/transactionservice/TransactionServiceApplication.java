package io.github.matiasmazzu.transactionservice;

import io.github.matiasmazzu.transactionservice.adapter.out.persistence.InMemoryTransactionRepository;
import io.github.matiasmazzu.transactionservice.application.TransactionService;
import io.github.matiasmazzu.transactionservice.application.port.TransactionRepository;
import io.github.matiasmazzu.transactionservice.domain.CycleChecker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class TransactionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransactionServiceApplication.class, args);
    }

    @Bean
    CycleChecker cycleChecker() {
        return new CycleChecker();
    }

    @Bean
    TransactionRepository transactionRepository() {
        return new InMemoryTransactionRepository();
    }

    @Bean
    TransactionService transactionService(TransactionRepository repository, CycleChecker cycleChecker) {
        return new TransactionService(repository, cycleChecker);
    }
}
