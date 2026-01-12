package com.example.payment.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI paymentServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Payment Service API")
                        .description("Payment microservice API for two-phase payment processing. " +
                                "Supports authorize, capture, and void operations.")
                        .version("1.0.0")
                        .contact(new Contact().name("Order System Team")))
                .servers(List.of(
                        new Server().url("http://localhost:8082").description("Local development server")
                ));
    }
}
