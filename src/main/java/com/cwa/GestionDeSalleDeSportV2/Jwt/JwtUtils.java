package com.cwa.GestionDeSalleDeSportV2.Jwt;

import com.cwa.GestionDeSalleDeSportV2.Service.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtils {

    @Value("${app.secret-key}")
    private String secretKet;

    @Value("${app.expiration-time}")
    private String expirationTime;

    public String generateToken(UserDetails userDetails) {

        Map<String, Object> claims = new HashMap<>();

        // ✅ Récupération du rôle
        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(auth -> auth.getAuthority())
                .orElse("ROLE_UTILISATEUR");

        claims.put("role", role);

        // ✅ Si c’est bien notre CustomUserDetails, on peut aussi récupérer l’ID
        if (userDetails instanceof CustomUserDetails) {
            Long id = ((CustomUserDetails) userDetails).getId();
            claims.put("id", id);
        }

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername()) // subject = téléphone
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + Long.parseLong(expirationTime)))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getSignKey() {
        byte[] keyByte = secretKet.getBytes();
        return new SecretKeySpec(keyByte, SignatureAlgorithm.HS256.getJcaName());
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpirationDate(token).before(new Date());
    }

    public String extractUsername(String token) {
        return extractClaims(token, Claims::getSubject);
    }

    public Long extractUserId(String token) {
        return extractAllClaims(token).get("id", Long.class);
    }

    private Date extractExpirationDate(String token) {
        return extractClaims(token, Claims::getExpiration);
    }

    private <T> T extractClaims(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(getSignKey().getEncoded()))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
