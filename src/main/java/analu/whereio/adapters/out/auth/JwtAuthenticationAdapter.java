package analu.whereio.adapters.out.auth;

import analu.whereio.application.model.ParsedAccessToken;
import analu.whereio.application.model.UserAccount;
import analu.whereio.application.ports.out.JwtAuthenticationPort;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Component
public class JwtAuthenticationAdapter implements JwtAuthenticationPort {

    private final SecretKey key;
    private final long accessExpirationMs;

    public JwtAuthenticationAdapter(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-expiration-ms}") long accessExpirationMs) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("app.jwt.secret must be at least 32 bytes for HS256");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
        this.accessExpirationMs = accessExpirationMs;
    }

    @Override
    public String generateAccessToken(UserAccount user) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + accessExpirationMs);
        List<String> roles = user.getRoles() == null || user.getRoles().isEmpty()
                ? List.of("USER")
                : user.getRoles();
        return Jwts.builder()
                .subject(user.getId())
                .claim("email", user.getEmail())
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(exp)
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    @Override
    public ParsedAccessToken parseAccessToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        String userId = claims.getSubject();
        String email = claims.get("email", String.class);
        @SuppressWarnings("unchecked")
        List<String> roles = claims.get("roles", List.class);
        if (roles == null || roles.isEmpty()) {
            roles = List.of("USER");
        }
        return new ParsedAccessToken(userId, email != null ? email : "", roles);
    }
}
