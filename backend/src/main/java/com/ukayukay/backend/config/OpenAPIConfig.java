package com.ukayukay.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development"),
                        new Server().url("https://api.ukayukay.com").description("Production")
                ))
                .info(new Info()
                        .title("UKay UKay E-Commerce API")
                        .version("1.0.0")
                        .description("Comprehensive REST API documentation for the UKay UKay e-commerce platform, supporting buyers, sellers, products, orders, payments, and business management.")
                        .contact(new Contact()
                                .name("UKay UKay Support")
                                .email("support@ukayukay.com")
                                .url("https://www.ukayukay.com")
                        )
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")
                        )
                );
    }
}
