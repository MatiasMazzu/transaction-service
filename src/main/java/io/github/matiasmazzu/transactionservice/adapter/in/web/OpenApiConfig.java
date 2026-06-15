package io.github.matiasmazzu.transactionservice.adapter.in.web;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI transactionServiceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Transaction Service API")
                .description("Registers transactions over a parent-child graph and computes transitive sums.")
                .version("0.0.1"));
    }
}
