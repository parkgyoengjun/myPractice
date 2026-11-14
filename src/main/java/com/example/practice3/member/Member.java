package com.example.practice3.member;

import jakarta.persistence.*;

@Entity // DB 테이블과 연결( @Entity 가 있어야 JPA 가 테이블로 만듬 )
@Table(name = "members") // 테이블 이름 정의
public class Member {
    // 클래스네임을 User 로 쓰지 않은 이유 : User테이블 이름으로 쓰면 H2 에서 에러가 뜸, 예약어 취급할수도 있다.

    @Id // 기본키(PK) 지정 : JPA 엔티티는 반드시 PK 가 필요함
    @GeneratedValue(strategy = GenerationType.IDENTITY) // PK를 DB가 자동 증가값으로 생성
    private Long id; // 래퍼런스 타입 : 아직 저장 전에는 id 가 없으므로 null 여야해서 Long

    @Column(nullable = false, unique = true)
    // @Column : 테이블의 컬럼과 연결하고, 컬럼의 조건을 지정
    // nullable : NOT NULL 제약 - 값이 비어 있는 계정 생성을 DB 수준에서 막는다.
    // unique = true : 중복금지 - 같은게 있을 경우 DB에서 거부
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role;

    protected Member() {
        // JPA 가 객체를 만들때 필수, 외부에서는 쓰지 못하게 protected 로 제한
    }

    public Member(String username,String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }
}///
