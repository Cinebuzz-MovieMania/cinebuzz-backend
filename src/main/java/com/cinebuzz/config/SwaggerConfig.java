package com.cinebuzz.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI cinebuzzOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Cinebuzz API")
                        .description("Movie ticket booking system")
                        .version("v1.0"));
    }
}