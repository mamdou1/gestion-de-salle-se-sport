package com.cwa.GestionDeSalleDeSportV2.Jwt;


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

    public String generateToken(UserDetails userDetails){

        Map<String, Object> claims = new HashMap<>();

        String role = userDetails.getAuthorities().stream()
                        .findFirst()
                                .map(auth -> auth.getAuthority())
                                        .orElse("ROLE_UTILISATEUR");

        claims.put("role", role);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+ Long.parseLong(expirationTime)))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ici on ne fait que signer la clée par rapport au caractere de la clee definis dans application.properti
    private Key getSignKey() {
        byte[] keyByte = secretKet.getBytes();
        return new SecretKeySpec(keyByte, SignatureAlgorithm.HS256.getJcaName());
    }

    public boolean validateToken(String token, UserDetails userDetails){

        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && ! isTokenExpired(token);
    }

    // before(new Date()) est pour verifier que la date d'expiration n'est pas encore passé donc on verifie qu'il n'est pas inferieur a la date actuel
    private boolean isTokenExpired(String token) {
        return extractExpirationDate(token).before(new Date());
    }

    public String extractUsername(String token) {

        return extractClaims(token, Claims::getSubject);
    }
    private Date extractExpirationDate(String token){

        return extractClaims(token, Claims::getExpiration);
    }

    // cette method nous permettre d'extraite tous les claims du token et retour le recuperer celui que l'on cherche,que uniquement un username
    // <T>T <- veut dire k le type est de type générique, et qu'on va pouvoir utiliser peu import ce k l'on veut recuperer dans notre project
    // Function<claims, T> va permettre prendre le claims et le type pour en suite pouvoir les retourner
    private <T>T extractClaims(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(getSignKey().getEncoded()))
                .build().parseClaimsJws(token)
                .getBody();
    }



}
