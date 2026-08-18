package com.duastore.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${app.url:http://localhost:8080}")
    private String appUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .servers(List.of(new Server().url(appUrl).description("Default Server")))
                .info(new Info()
                        .title("DuaStore API")
                        .version("1.0.0")
                        .description("API documentation for DuaStore e-commerce platform")
                        .contact(new Contact()
                                .name("DuaStore Team")
                                .email("support@duastore.vn")
                                .url("https://duastore.vn"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://duastore.vn/license")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT Authorization header using the Bearer scheme. Example: \"Authorization: Bearer {token}\"")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .servers(List.of(
                        new Server().url(appUrl).description("Production Server"),
                        new Server().url("http://localhost:8080").description("Local Development Server")
                ));
    }
}