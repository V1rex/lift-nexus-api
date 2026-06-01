package com.v1rex.liftnexus.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
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
                .version("0.0.1-SNAPSHOT")
                .description(
                    "High-performance asynchronous optimization and dispatching engine for"
                        + " intralogistics assets using Timefold.")
                .contact(
                    new Contact()
                        .name("Mohamed Amine Bahij")
                        .email("medaminebahij02@gmail.com")
                        .url("https://github.com/v1rex"))
                .license(
                    new License()
                        .name("Apache License 2.0")
                        .url("http://www.apache.org/licenses/LICENSE-2.0")))
        .servers(
            List.of(
                new Server().url("http://localhost:8080").description("Local Development Server")))
        .tags(
            List.of(
                new Tag()
                    .name("Forklifts")
                    .description("Manage forklift assets and their operational status"),
                new Tag()
                    .name("Forklift Types")
                    .description("Manage forklift type catalog (models, capacity, equipment)"),
                new Tag()
                    .name("Storage Bins")
                    .description("Manage warehouse storage bin locations and coordinates"),
                new Tag()
                    .name("Load Units")
                    .description("Manage load units (pallets, containers) and their tracking"),
                new Tag()
                    .name("Transport Orders")
                    .description("Manage transport orders for moving loads across the warehouse"),
                new Tag()
                    .name("Dispatcher")
                    .description("Submit and manage Timefold optimization jobs")));
  }
}
