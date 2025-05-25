package app.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {

       Info applicationInfo = new Info()
                .title("Urlaubsplanung")
                .description("REST API- Benachrichtigungsmodus innerhalb der Urlaubsplanung Anwendung.\n" +
                        "Es dient der automatisierten Kommunikation zwischen System, Mitarbeitern, Vorgesetzten und der HR-Abteilung.")
                .version("2.0")
                .contact(new Contact()
                        .name("Hristo Ivanov")
                        .email("hristo.ivanov@top-logic.com")
                        .url("http://45.10.99.48:8080"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT"));
        return new OpenAPI().info(applicationInfo);
    }
}
