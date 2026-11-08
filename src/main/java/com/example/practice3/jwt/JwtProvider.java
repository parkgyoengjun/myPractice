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
// 비밀키와 만료시간을 한 번만 읽어서 보관, 로그인 API, 인증 필터 등 여러곳에서 같은 객체를 공유해야한다.
// 빈으로 등록하면 다른 클래스가 생성자에 JwtProvider를 선언하는 것만으로 주입받을 수 있다.
// 테스트의 @Autowired JwtProvider jwtProvider 도 이 때문에 동작
public class JwtProvider { // 토큰 전담 클래스

    private final SecretKey secretKey; // 서명, 검증에 쓰는 비밀키(signature)
    // 매 요청마다 문자열을 디코딩하지 않고, 생성자에서 한 번만 변환한 결과를 재사룡
    private final long accessTokenExpirationMs; // 만료시간 int 로는 큰 시간 값이 넘칠 수 있어서
    // Long ( 래퍼런스 객체 ), long( 기본형 ) Long 은 값이 없을 수 있고, long 은 값이 항상 있어야함

    public JwtProvider(@Value("${jwt.secret}") String secret,
                       @Value("${jwt.access-token-expiration-ms}") long accessTokenExpirationMs) {
        // @Value : 비밀키를 코드에 직접 쓰지 않고 yaml 에 있는 것을 주입 받음
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        // Keys.hmacShaKeyFor()
        // - 바이트 배열을 secretKey 로 만들고 길이를 검사, 32 바이트 미만이면 예외를 던져 키를 막음
        // Decoders.BASE64.decode(secret)
        // - base64 텍스트를 바이트 배열로 변환, 설정 파일에는 텍스트만 쓸 수 있으니 바이트 키를
        // 텍스트(base64) 로 저장했다가 되돌린다.
        this.accessTokenExpirationMs = accessTokenExpirationMs;

        /**
         * yml의 문자열 "sk8OuYMg...miQ="            (Base64로 표현된 텍스트)
         *       │  Decoders.BASE64.decode(...)
         *       ▼
         * byte[] (32바이트의 원래 데이터)
         *       │  Keys.hmacShaKeyFor(...)           ← 여기서 길이 검사(32바이트 이상)
         *       ▼
         * SecretKey 객체                               → this.secretKey에 저장
         * -> yaml에 JWT 정보(BASE64) 를 바이트로 변환 후 검사
         */
    }

    public String createAccessToken(String username, String role) {
        // 로그인 성공시 토큰 생성 메서드
        Date now = new Date(); // 현재 시각을 넣기 위해
        Date expiry = new Date(now.getTime() + accessTokenExpirationMs);
        // Date 는 밀리초로 계산, JWT 의 iat/exp 는 JJWT가 초 단위로 변환해서 기록

        return Jwts.builder() // 토큰 생성 시점
                // header 을 생성하는데 header 은 토큰인증방식 이니까
                // signWith(secretKey) 가 서명위조와 비밀키를 사용해 signature 를 생성하여
                // 그 알고리즘 정보 또한 header 에 영향을 갈 수 밖에 없다.
                /**
                 * JWT 와 Jwts 다름
                 * - JWT 는 JSON WEB TOKEN 이라는 토큰 표준
                 * - Jwts 는 java 에서 JWT 를 다루기 위한 라이브러리의 클래스
                 *
                 * Jwts 사용이유
                 * - java 환경에서 JWT 를 생성하고 검증하는 기능을 제공
                 * - 빌더 패턴을 지원해서 코드를 직관적으로 작성
                 * - 토큰의 Subject, Claim, 발급시각, 만료 시간 등을 쉽게 설정
                 * - 서명(Signature) 을 추가하는 기능 제공
                 */
                .subject(username) // 표준 claim sub 설정, 토큰 주인 표시( payload )
                .claim("role", role) // 직접 정한 claim 추가, 표준에 없는 정보(역할) 저장( payload )
                .issuedAt(now) // 발급 시각 기록( payload )
                .expiration(expiry) // ( payload )
                .signWith(secretKey) // ( signature )
                // 비밀키를 이용해서 서명위조를 만든다. 서명추가, 서명이 없으면 누구나 위조 가능
                // 키 길이를 보고 알고리즘을 자동 선택, 32바이트 키는 HS256 이된다.
                .compact(); // 최종 토큰 문자열 얻기

        // JJWT 라이브러리가 header 의 정보를 자동으로 구성 -> 직접 구성할수 도 있음
        /**
         * claim 은 payload 구성요소들
         *  claim 의 크게 3 종류
         *      -  Registered Claims ( 등록된 클레임 )
         *          - JWT 표준에서 저으이한 클레임
         *          - ex) sub, exp, iat, iss, aud 등
         *      -  Public Claim ( 공개 클레임 )
         *          - 공개적으로 사용할 수 있도록 정의한 클레임
         *
         *      - Private Claim ( 비공개 클레임 )
         *           - 토큰을 사용하는 시스템 사이에서 자체적으로 정의해 사용하는 클레임
         *           - ex) role, username, userId
         */
    }

    public Claims parseClaims(String token) {   // parseSignedClaims : 서명과 만료(exp)를 함께 검증, 단순 디코딩이 아님
        return Jwts.parser()// 토큰 읽기 준비, 검증의 출발점
                .verifyWith(secretKey) // 서명 검증용 키 지정, 서명할 때 쓴 키와 같은 키여야한다.
                .build() // 파서 완성
                .parseSignedClaims(token)
                // 토큰을 읽으면서 서명과 만료 검증, 검증 없이 읽기만 하는것과 다름
                .getPayload(); // 검증된 claim 묶음 꺼내기
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
        // String 타입 이라는 정보를 담는다.
        // role claim 을 꺼내되 String 으로 돌려달라 는 뜻
    }
}///
