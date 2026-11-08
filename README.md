1. JWT ( json web token ) 는 " 누가 누군인지, 언제까지 유효한지 " 를 담은 문자열에 서버의 서명을 붙인 토큰, 표준 RFC7519 이다
   핵심은 서버가 로그인 상태를 저장하지 않는다.
   비유하면 세션 - 서버가 가진 출입자 명단과 대조하는 방식
   jwt - 위조 불가능한 도장이 찍힌 출입증을 들고 오는 방식

2. JWT 의 구조
   점( . ) 으로 구분된 3부분
   xxxxx.yyyy.zzzzz
   header.Payload.Signature

   *header : 서명 알고리즘 정보
   json { "alg": "HS256", "typ": "JWT" }

   *payload
   json { "sub": "1234",  "name": "hong", "role": "USER", "iat": 1600000, "exp": 160003600  }
   sub : 토큰 주체 ( 보통 사용자 ID )
   iat : 발급시각
   exp: 만료시각
   name, role : 은 직접 정한 항목 ( 커스텀 claim )

    * signature : 위조 방지용 서명
      HMAC-SH256( base64url(header) + "," + vase64url (payload), 서버만 아는 비밀키 )

header 와 payload 는 각각 json 을 base64url 로 인코딩한 것이고, 세 부분을 . 으로 이어 붙이면 토큰이 된다.

반드시 알아야할 점
-  payload 는 암호화가 아니라 인코딩, 누구나 디코딩해서 내용을 볼수 있다. 비밀번호, 주민번호 같은 민감정보를 넣으면 안됨
- 서명은 " 내용이 바뀌지 않았음"을 보장, 누가 payload 의 role 을 admin 으로 바꾸면 서명이 맞지 않아 서버가 거부, 비밀키를 모르면 올바른 서명을 만들 수 없다.

3. 동작 흐름
    1. 클라이언트 -> 서버  POST/login ( 아이디, 비밀번호 )
    2. 서버 			비밀번호 확인 -> jwt 생성( 서명 ) -> 응답으로 전달
    3. 클라이언트		토큰을 보관
    4. 클라이언트 -> 서버 	요청마다 헤더에 첨부
       authorization : bearer < 토큰 >
    5. 서버 			서명 검증 + 만료 시작 확인 -> 통과하면 요청 처리

세션과의 차이는 5번 이다. 세션은 저장소를 조회하지만, JWT 는 계산만으로 검증, 서버가 여러 대여도 같은 비밀키만 공휴하면 어느 서버든 검증할 수 있다.

4. 어떤 때 쓰는가
   적합한 경우
- 프론트엔드( react, vue 등 )와 백엔드가 분리된 rest api
- 모바일 앱 백엔드 ( 쿠키 기반 세션이 불편한 환경 )
- 서버가 여러 대이거나 msa 구조라 세션 공유가 부담인 경우
- 서버 간 인증 정보 전달

필요 없는 경우
- 서버에서 화면까지 렌더링하는 단순한 웹 애플리케이션, 세션이 더 단순하고 안전하게 관리됨
- 로그아웃/ 강제 로그아웃을 즉시 반영해야하는 시스템

jwt 가 세션보다 무조건 우월한 것은 아니다. 구조에 따른 선택

5. 문제점과 해결방법
    1. 문제 : 발급한 토큰을 강제로 무효화하기 어렵다 - 서버가 상태를 저장하지 않으므로 만료 전까지 유효
       Access Token  수명을 짧게( 15-30분 ) + Refresh Token 병행, 필요 시 블랙리스트 (Redis등)를
       두되, 이 경우 stateless 의 장점이 줄어든다.

    2. 문제 : 토큰 탈취 - 탈취되면 만료 전까지 누구나 사용가능
       HTTPS 필수, 짧은 수명, Refresh Token 재발급 시 교체( rotation )

    3. 문제 :  클라이언트 저장위치 - localStorage 는 Xss에 취약, 쿠키는 CSRF 고려 필요
       HttpOnly + Secure + SameSite  쿠키에 저장하는 방식이 권장되지만, 정답이 하나로 정해지것은 아니며 구조에 따라 선택이 갈린다.

    4. payload 노출 : 내용을 누구나 읽을 수 있음
       민감 정보 미포함

    5. 토큰 크기 : claim 이 많으면 매 요청해더가 커딘다.
       필요한  claim 만 포함

    6. 검증 설정 실수 : alg: none 허용, 알고리즘 혼동 같은 취약점이 과거에 실제로 존재
       검증 시 허용 알고리즘을 서버에서 고정, 검증된 라이브러리 사용



jwt:
secret: sk8OuYMgyyH3Jy8aPRsJiHCEdKeaM5gaowhf7SL3miQ=
access-token-expiration-ms: 1800000

-> 학습용 키
- 이 값은 base64 디코딩하면 정확히 32바이트(256비트)다. HS256 최소 요건을 충족
- 키를 직접 만들 때 문자열 길이가 아니라 디코딩 후 바이트 길이가 32이상이어야 한다.
base64 문자 32개는 24바이트라서 WeakKeyException 이 난다.
- 1800000 dms 30분(밀리초)
- 실제 배포 시에는 키를 설정 파일에 두지 않고 환경변수로 분리


    /**
     *  Claims  pareClaims 의 반환 타입  토큰 payload(claims 모음)를 담느 JJWT 타입
     *  JwtException    validateToken 의 catch   JJWT 가 던지는 검증 실패 예외의 최상위 부모
     *  Jwts    토큰 생성/피싱의 시작점   Jwts.builder(),Jwts.parser()
     *  Decoders    생성자     base64 문자열을 byte 배열로 변환
     *  Keys        생성자     byte 배열로 HMAC용 키 객체 생성
     *  Value   생성자파라미터     설정ㅍ일 값을 주입
     *  Component   클래스선언부      스프링빈으로 등록
     *  SecretKey   필드타입        대칭키( HMAC ) 표준 자바 타입
     *  Date        토큰생성        iat,exp 시각 계산
     */

