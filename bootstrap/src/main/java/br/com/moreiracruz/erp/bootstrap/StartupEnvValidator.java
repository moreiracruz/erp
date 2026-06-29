package br.com.moreiracruz.erp.bootstrap;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class StartupEnvValidator {

    private final Environment environment;

    public StartupEnvValidator(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void validate() {
        String jwtSecret = environment.getProperty("jwt.secret");
        if (jwtSecret == null || jwtSecret.isBlank()) {
            throw new IllegalStateException("Required configuration property jwt.secret is not set");
        }

        String dbPassword = environment.getProperty("spring.datasource.password");
        if (dbPassword == null || dbPassword.isBlank()) {
            throw new IllegalStateException("Required configuration property spring.datasource.password is not set");
        }
    }
}
