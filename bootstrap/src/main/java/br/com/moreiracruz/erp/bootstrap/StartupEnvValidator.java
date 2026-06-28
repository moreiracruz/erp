package br.com.moreiracruz.erp.bootstrap;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class StartupEnvValidator {

    @PostConstruct
    public void validate() {
        String jwtSecret = System.getenv("JWT_SECRET");
        if (jwtSecret == null || jwtSecret.isBlank()) {
            throw new IllegalStateException("Required environment variable JWT_SECRET is not set");
        }

        String dbPassword = System.getenv("SPRING_DATASOURCE_PASSWORD");
        if (dbPassword == null || dbPassword.isBlank()) {
            throw new IllegalStateException("Required environment variable SPRING_DATASOURCE_PASSWORD is not set");
        }
    }
}
