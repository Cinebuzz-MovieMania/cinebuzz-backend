package com.cinebuzz.security;

import com.cinebuzz.exception.ValidationException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    public static final String CLAIM_PURPOSE = "purpose";
    public static final String PURPOSE_LOGIN = "LOGIN";
    public static final String PURPOSE_REGISTRATION_PROOF = "REGISTRATION_PROOF";

    @Value("${jwt.secret}")
    private String secret;

    /** Lifetime of new tokens (ms): issued in {@link #generateToken} as the {@code exp} claim. */
    @Value("${jwt.expiration}")
    private Long expiration;

    /** Short-lived JWT after email OTP verification; used only for POST /register/complete. */
    @Value("${jwt.registration-proof-expiration-ms:900000}")
    private Long registrationProofExpirationMs;

    /** Parser tolerance (seconds) for {@code exp}/{@code nbf}; does not extend real lifetime, only comparison slack. */
    @Value("${jwt.clock-skew-seconds:60}")
    private long clockSkewSeconds;

    private JwtParser jwtParser;

    @PostConstruct
    void buildParser() {
        jwtParser = Jwts.parser()
                .verifyWith((SecretKey) getSignKey())
                .clockSkewSeconds(clockSkewSeconds)
                .build();
    }

    // Generate token for a user
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_PURPOSE, PURPOSE_LOGIN);
        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignKey())
                .compact();
    }

    /** Token proving email ownership; not a login session — do not use as Bearer for protected APIs. */
    public String generateRegistrationProofToken(String normalizedEmail) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_PURPOSE, PURPOSE_REGISTRATION_PROOF);
        return Jwts.builder()
                .claims(claims)
                .subject(normalizedEmail)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + registrationProofExpirationMs))
                .signWith(getSignKey())
                .compact();
    }

    /** Returns subject email if token is a valid registration-proof token; otherwise throws {@link ValidationException}. */
    public String parseAndValidateRegistrationProofToken(String token) {
        try {
            Claims claims = jwtParser.parseSignedClaims(token).getPayload();
            if (!PURPOSE_REGISTRATION_PROOF.equals(claims.get(CLAIM_PURPOSE, String.class))) {
                throw new ValidationException("Invalid registration session");
            }
            return claims.getSubject();
        } catch (JwtException e) {
            throw new ValidationException("Invalid or expired registration session");
        }
    }

    /** Purpose claim, if present; {@code null} for legacy tokens without the claim (treated as login). */
    public String extractPurpose(String token) {
        try {
            return extractClaim(token, c -> c.get(CLAIM_PURPOSE, String.class));
        } catch (JwtException e) {
            return null;
        }
    }

    // Extract email from token
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Check if token is valid
    public boolean isTokenValid(String token, UserDetails userDetails) {
        String email = extractEmail(token);
        return email.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = jwtParser.parseSignedClaims(token).getPayload();
        return claimsResolver.apply(claims);
    }

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}