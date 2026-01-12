package com.example.order.infrastructure.config;

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
    public OpenAPI orderServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Order Service API")
                        .description("Order microservice API for creating and querying orders. " +
                                "Implements CQRS pattern with separate command and query endpoints.")
                        .version("1.0.0")
                        .contact(new Contact().name("Order System Team")))
                .servers(List.of(
                        new Server().url("http://localhost:8081").description("Local development server")
                ));
    }
}
