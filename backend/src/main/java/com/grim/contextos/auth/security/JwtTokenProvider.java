package com.grim.contextos.auth.security;


import com.grim.contextos.auth.model.UserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private final SecretKey accessKey;
    private final SecretKey refreshKey;
    private final long acceessExpiration;
    private final long refreshExpiration;


    public JwtTokenProvider(@Value("${jwt.secret}") String secret) {
        // Use the secret directly - HMAC-SHA512 needs at least 64 bytes
        // If the provided secret is shorter, we hash it to derive a proper key
        byte[] keyBytes = secret.getBytes();
        if (keyBytes.length < 64) {
            // Derive a 64-byte key from the secret using SHA-256 hashing
            try {
                java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
                byte[] hash = md.digest(secret.getBytes());
                // Expand to 64 bytes by doubling
                byte[] expanded = new byte[64];
                System.arraycopy(hash, 0, expanded, 0, 32);
                System.arraycopy(hash, 0, expanded, 32, 32);
                keyBytes = expanded;
            } catch (Exception e) {
                // Fallback: just pad with zeros
                byte[] padded = new byte[64];
                System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
                keyBytes = padded;
            }
        }
        this.accessKey = Keys.hmacShaKeyFor(keyBytes);
        this.refreshKey = Keys.hmacShaKeyFor(keyBytes); // Same key source, different salt in claims
        this.acceessExpiration = 1000L * 60 * 15;
        this.refreshExpiration = 1000L * 60 * 60 * 24 * 7; // 7 days
    }


    public String generateAcessToken(UserPrincipal user){
        return Jwts.builder()
                .subject(user.id().toString())
                .claim("email",user.email())
                .claim("role",user.role().name())
                .claim("type","ACCESS")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis()+acceessExpiration))
                .signWith(accessKey)
                .compact();
    }

    public String generateRefreshToken(UserPrincipal user){
        return Jwts.builder()
                .subject(user.id().toString())
                .id(UUID.randomUUID().toString())
                .claim("type","REFRESH")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis()+refreshExpiration))
                .signWith(refreshKey)
                .compact();
    }

    public boolean validateToken(String token){
        try{
            Jwts.parser()
                    .verifyWith(accessKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        }catch(JwtException ex){
            return false;
        }
    }

    public UUID getUserIdFromToken(String token){
        Claims claims=Jwts.parser()
                .verifyWith(accessKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return UUID.fromString(claims.getSubject());
    }

    public String getEmailFromToken(String token){
        Claims claims=Jwts.parser()
                .verifyWith(accessKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.get("email",String.class);

    }

}
