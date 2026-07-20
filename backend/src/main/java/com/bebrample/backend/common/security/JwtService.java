package com.bebrample.backend.common.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final String secret = "govnogovnogovnogovnogovnogovnogovno";

    private final long expirationMs = 86400000;

    public String generateToken(String id, String username, String role) {
        return JWT.create()
                .withSubject(id)
                .withClaim("username", username)
                .withClaim("role", role)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + expirationMs))
                .sign(Algorithm.HMAC256(secret));
    }

    public boolean verifyToken(String token) {
        JWT.require(Algorithm.HMAC256(secret))
                .build()
                .verify(token);
        return true;
    }

}
