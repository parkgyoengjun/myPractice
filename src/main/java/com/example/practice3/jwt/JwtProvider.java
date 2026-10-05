package com.example.practice3.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
// 스프링이 직접 객체로 만들어 관리하는 빈 등록( 컨트롤러,서비스,레포지토리 중 어느 역할도 아니라서 범용 @Component 사용)
// 비밀키와 만료시간을 한 번만 ㅇㄺ어서 보관, 로그인 API, 인증 필터 등 여러곳에서 같은 객체를 공유해야한다.
// 빈으로 등록하면 다른 클래스가 생성자에 JwtProvider를 선언하는 것만으로 주입받을 수 있다.
// 테스트의 @Autowired JwtProvider jwtProvider 도 이 때문에 동작
public class JwtProvider {

    private final SecretKey secretKey; // 매 요청마다 문자열을 디코딩하지 않고, 생성자에서 한 번만 변환한 결과를 재사룡
    private final long accessTokenExpirationMs;

    public JwtProvider(@Value("${jwt.secret}") String secret,
                       @Value("${jwt.access-token-expiration-ms}") long accessTokenExpirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.accessTokenExpirationMs = accessTokenExpirationMs;
    }

    public String createAccessToken(String username, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpirationMs);
        // Date 는 밀리초로 계산, JWT 의 iat/exp 는 JJWT가 초 단위로 변환해서 기록

        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                // signWith(secretKey): 키 길이를 보고 알고리즘을 자동 선택, 32바이트 키는 HS256 이된다.
                .compact();
    }

    public Claims parseClaims(String token) {   // parseSignedClaims : 서명과 만료(exp)를 함께 검증, 단순 디코딩이 아님
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token) {
        // 서명위조, 만료, 형식오류, 빈 문자열(IllegalArgumentException)을 모두 false 처리

        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    public String getUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public String getRole(String token) {
        return parseClaims(token).get("role", String.class);
    }
}///
