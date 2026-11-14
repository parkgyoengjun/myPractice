package com.example.practice3.repository;

import com.example.practice3.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// @Repository 쓰지 않은 이유 : JpaRepository 를 상속한 인터페이스는 Spring Date 가 자동으로 빈으로 등록
public interface MemberRepository extends JpaRepository<Member, Long> {
    // interface 를 쓰는 이유 : 구현코드를 직접 쓰지 않는다. Spring Date JPA 가 실행 시점에 구현체를 자동 생성
    // extend JpaRepository : save, findById, findAll, delete 등 자동제공
    //                        JpaRepository<엔티티타입, PK타입> 이고, Long 은 엔티티의 id 타입과 일치해야함

    Optional<Member> findByUsername(String username);
    // Optional : null 방지 - 빈 Optional 을 반환

    boolean existsByUsername(String username);
}///
