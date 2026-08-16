package dev.incidentai.backend.security;

import dev.incidentai.backend.repository.UserAccountRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class TokenAuthenticationFilter extends OncePerRequestFilter {
    private final TokenService tokens;
    private final UserAccountRepository users;

    public TokenAuthenticationFilter(TokenService tokens, UserAccountRepository users) {
        this.tokens = tokens;
        this.users = users;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        var authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ") && SecurityContextHolder.getContext().getAuthentication() == null) {
            var username = tokens.validateAndGetUsername(authorization.substring(7));
            if (username != null) {
                users.findByUsernameIgnoreCase(username).filter(u -> u.isEnabled()).ifPresent(user -> {
                    var principal = new UserPrincipal(user);
                    var authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                });
            }
        }
        chain.doFilter(request, response);
    }
}
