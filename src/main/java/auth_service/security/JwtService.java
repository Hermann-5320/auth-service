package auth_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration}")
    private Long expiration;

    // Générer un token JWT
    public String genererToken(String email, String role, Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("userId", userId);

        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getCle())
                .compact();
    }

    // Extraire l'email du token
    public String extraireEmail(String token) {
        return extraireClaims(token).getSubject();
    }

    // Extraire le rôle du token
    public String extraireRole(String token) {
        return extraireClaims(token).get("role", String.class);
    }

    // Extraire l'userId du token
    public Long extraireUserId(String token) {
        return extraireClaims(token).get("userId", Long.class);
    }

    // Vérifier si le token est valide
    public boolean estValide(String token, String email) {
        return extraireEmail(token).equals(email) && !estExpire(token);
    }

    // Vérifier si le token est expiré
    private boolean estExpire(String token) {
        return extraireClaims(token).getExpiration().before(new Date());
    }

    // Extraire tous les claims
    private Claims extraireClaims(String token) {
        return Jwts.parser()
                .verifyWith(getCle())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Générer la clé de signature
    private SecretKey getCle() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
}