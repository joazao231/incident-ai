package dev.incidentai.backend.service;

import dev.incidentai.backend.dto.*;
import dev.incidentai.backend.entity.*;
import dev.incidentai.backend.exception.BusinessException;
import dev.incidentai.backend.repository.UserAccountRepository;
import dev.incidentai.backend.security.TokenService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service @Transactional
public class AuthService {
    private final UserAccountRepository users;
    private final PasswordEncoder passwords;
    private final TokenService tokens;

    public AuthService(UserAccountRepository users, PasswordEncoder passwords, TokenService tokens) {
        this.users = users;
        this.passwords = passwords;
        this.tokens = tokens;
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        var user = users.findByUsernameIgnoreCase(request.username().trim())
                .filter(UserAccount::isEnabled)
                .orElseThrow(() -> new BadCredentialsException("Usuário ou senha inválidos"));
        if (!passwords.matches(request.password(), user.getPasswordHash()))
            throw new BadCredentialsException("Usuário ou senha inválidos");
        return new AuthResponse(tokens.generate(user), "Bearer", tokens.expirationSeconds(), user.getId(),
                user.getName(), user.getUsername(), user.getRole());
    }

    public UserResponse create(UserRequest request) {
        var username = request.username().trim().toLowerCase();
        if (users.existsByUsernameIgnoreCase(username)) throw new BusinessException("Nome de usuário já cadastrado");
        var user = UserAccount.builder().name(request.name().trim()).username(username)
                .passwordHash(passwords.encode(request.password())).role(request.role()).enabled(true).build();
        return UserResponse.from(users.save(user));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return users.findAll().stream().map(UserResponse::from).toList();
    }
}
