package com.v1rex.liftnexus.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("LiftNexus API Engine")
                .version("0.0.1")
                .description(
                    "High-performance asynchronous optimization and dispatching engine for intralogistics assets using Timefold."))
        .servers(
            List.of(
                new Server().url("http://localhost:8080").description("Local Development Server")));
  }
}
