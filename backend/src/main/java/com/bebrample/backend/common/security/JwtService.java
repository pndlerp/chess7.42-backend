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

    public String generateToken(String username){
        return JWT.create()
                .withSubject(username)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + expirationMs))
                .sign(Algorithm.HMAC256(secret));
    }

    public String getUsernameAndVerify(String token){
        return JWT.require(Algorithm.HMAC256(secret))
                .build()
                .verify(token)

                .getSubject();
    }
}
