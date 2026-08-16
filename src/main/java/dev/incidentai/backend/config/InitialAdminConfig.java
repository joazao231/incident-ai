package dev.incidentai.backend.config;

import dev.incidentai.backend.entity.*;
import dev.incidentai.backend.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class InitialAdminConfig {
    @Bean
    ApplicationRunner createInitialAdmin(UserAccountRepository users, PasswordEncoder passwords,
            @Value("${incident-ai.security.initial-admin-name:Administrador}") String name,
            @Value("${incident-ai.security.initial-admin-username:admin}") String username,
            @Value("${incident-ai.security.initial-admin-password:admin123}") String password) {
        return args -> {
            if (!users.existsByUsernameIgnoreCase(username)) {
                users.save(UserAccount.builder().name(name).username(username.toLowerCase())
                        .passwordHash(passwords.encode(password)).role(UserRole.ADMIN).enabled(true).build());
            }
        };
    }
}
