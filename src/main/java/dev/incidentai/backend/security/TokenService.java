package dev.incidentai.backend.security;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import dev.incidentai.backend.entity.UserAccount;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;

@Service
public class TokenService {
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();
    private final ObjectMapper mapper;
    private final byte[] secret;
    private final long expirationSeconds;

    public TokenService(ObjectMapper mapper,
                        @Value("${incident-ai.security.jwt-secret}") String secret,
                        @Value("${incident-ai.security.token-expiration-seconds:28800}") long expirationSeconds) {
        if (secret.length() < 32) throw new IllegalStateException("JWT_SECRET deve possuir pelo menos 32 caracteres");
        this.mapper = mapper;
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.expirationSeconds = expirationSeconds;
    }

    public String generate(UserAccount user) {
        try {
            var now = Instant.now().getEpochSecond();
            var header = ENCODER.encodeToString(mapper.writeValueAsBytes(Map.of("alg", "HS256", "typ", "JWT")));
            var payload = ENCODER.encodeToString(mapper.writeValueAsBytes(Map.of(
                    "sub", user.getUsername(), "role", user.getRole().name(), "iat", now, "exp", now + expirationSeconds)));
            var content = header + "." + payload;
            return content + "." + ENCODER.encodeToString(sign(content));
        } catch (Exception e) {
            throw new IllegalStateException("Não foi possível gerar o token", e);
        }
    }

    public String validateAndGetUsername(String token) {
        try {
            var parts = token.split("\\.");
            if (parts.length != 3) return null;
            var content = parts[0] + "." + parts[1];
            if (!MessageDigest.isEqual(sign(content), DECODER.decode(parts[2]))) return null;
            Map<String, Object> claims = mapper.readValue(DECODER.decode(parts[1]), new TypeReference<>() {});
            var expiration = ((Number) claims.get("exp")).longValue();
            if (Instant.now().getEpochSecond() >= expiration) return null;
            return Objects.toString(claims.get("sub"), null);
        } catch (Exception e) {
            return null;
        }
    }

    public long expirationSeconds() { return expirationSeconds; }

    private byte[] sign(String content) throws Exception {
        var mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret, "HmacSHA256"));
        return mac.doFinal(content.getBytes(StandardCharsets.UTF_8));
    }
}
