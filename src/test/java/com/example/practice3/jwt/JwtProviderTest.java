package com.example.practice3.jwt;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JwtProviderTest {

    @Autowired
    JwtProvider jwtProvider;

    @Value("${jwt.secret}")
    String secret;

    @Test
    void 토큰_생성_후_검증과_claim_확인() {
        String token = jwtProvider.createAccessToken("hong", "USER");
        System.out.println("TOKEN = " + token);

        assertTrue(jwtProvider.validateToken(token));

        Claims claims = jwtProvider.parseClaims(token);
        assertEquals("hong", claims.getSubject());
        assertEquals("USER", claims.get("role", String.class));
        assertNotNull(claims.getExpiration());
    }

    @Test
    void 서명이_변조된_토큰은_검증_실패() {
        String token = jwtProvider.createAccessToken("hong", "USER");
        String[] parts = token.split("\\.");
        String sig = parts[2];
        char flipped = sig.charAt(5) == 'A' ? 'B' : 'A';
        String tampered = parts[0] + "." + parts[1] + "."
                + sig.substring(0, 5) + flipped + sig.substring(6);

        assertFalse(jwtProvider.validateToken(tampered));
    }

    @Test
    void 만료된_토큰은_검증_실패() {
        JwtProvider expiredProvider = new JwtProvider(secret, -1000L);
        String expiredToken = expiredProvider.createAccessToken("hong", "USER");

        assertFalse(jwtProvider.validateToken(expiredToken));
    }
}///