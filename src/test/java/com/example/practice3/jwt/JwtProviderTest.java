package com.example.practice3.jwt;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
// 애플리케이션 전체를 띄워서 테스트 진행
// JwtProvider 가 @Value 로 설정(yaml)을 읽는 스프링 빈이라서, 설정 읽기와 빈 생성까지 함꼐 검증하기 위해
// 단점: 전체 컨텍스트를 띄우므로 느리다.
class JwtProviderTest {
    // public 이 없는 이유 : JUnit5 는 public 이 아닌 클래스와 메서드도 실행한다?
    // -> 이게 SpringBootTest 랑 연관이 있는건가??

    @Autowired
    JwtProvider jwtProvider;
    // 스프링이 만든 실제 빈을 주입
    // 테스트 클래스는 생성자 주입 대신 필드 주입을 쓰는것이 원칙

    @Value("${jwt.secret}")
    String secret;
    // 세 번째 테스트에서 같은 키를 쓰는 별도의 JwtProvider 를 만들기 위해 비밀키 문자열이 필요해서 받음
    // private 가 없는 이유 : 스프링이 주입 가능하고 테스트 클래스 안에서만 쓰기 때문에 접근 제한을
    // 강하게 둘 필요 없다.
    /**
     * yaml 파일에 있는게 String 으로 구성되어있기때문에?? -> 그러면 JwtProvider 의 SecretKey 는 무엇인가??
      */


    @Test
    void 토큰_생성_후_검증과_claim_확인() {
        String token = jwtProvider.createAccessToken("hong", "USER"); // 토큰 생성
        System.out.println("TOKEN = " + token);

        assertTrue(jwtProvider.validateToken(token));

        Claims claims = jwtProvider.parseClaims(token); // 검증하고 디코딩된 토큰
        assertEquals("hong", claims.getSubject()); // 디코딩된 토큰의 sub 부분 일치확인
        assertEquals("USER", claims.get("role", String.class)); // 디코딩된 토큰의 role 부분 일치확인
        // assertEquals(기대값, 실제값)
        assertNotNull(claims.getExpiration()); // 토큰에 만료 시각이 들어 있는가
    }

    @Test
    void 서명이_변조된_토큰은_검증_실패() {
        String token = jwtProvider.createAccessToken("park", "USER"); // 토큰 생성
        System.out.println(token + " 변경 전 @@@@@@@@@@@@@@@@@@@@@@@");

        String[] parts = token.split("\\."); // . 을 기준으로 나누기
        String sig = parts[2]; // header - 0, payload -1, signature - 2
        // 2 부분 signature 부분만 sig 에 넣기
        char flipped = sig.charAt(5) == 'A' ? 'B' : 'A'; // signature 의 5부분이 a,b 바꾸기
        String tampered = parts[0] + "." + parts[1] + "."
                + sig.substring(0, 5) + flipped + sig.substring(6);
        // sig.substring(0, 5) : 0-4 까지
        // sig.substring(6) : 6번부터 끝까지  -> 시작은 포함이고 끝은 미포함이다
        // 바꾼걸로 합치기
        System.out.println(tampered + " 변경 후 @@@@@@@@@"); // 변경된 토큰으로 검증

        assertFalse(jwtProvider.validateToken(tampered));
    }

    @Test
    void 만료된_토큰은_검증_실패() {
        JwtProvider expiredProvider = new JwtProvider(secret, -1000L);
        // 임의로 만료된 객체 생성

        String expiredToken = expiredProvider.createAccessToken("hong", "USER");
        // 생성된 객체로 token 생성

        assertFalse(jwtProvider.validateToken(expiredToken));
    }
}///