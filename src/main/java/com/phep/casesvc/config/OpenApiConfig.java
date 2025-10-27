package com.phep.casesvc.config;

// Add this class if you want custom info
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    OpenAPI api() {
        return new OpenAPI().info(new Info()
                .title("Case Service API")
                .version("v1")
                .description("CRUD + status updates for cases"));
    }
}
