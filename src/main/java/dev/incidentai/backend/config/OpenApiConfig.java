package dev.incidentai.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    OpenAPI incidentAiOpenApi() {
        return new OpenAPI().info(new Info().title("Incident AI API").version("1.0.0")
                .description("Monitora aplicações, registra verificações e gerencia incidentes."));
    }
}
