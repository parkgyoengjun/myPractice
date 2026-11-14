package com.example.practice3.repository;

import com.example.practice3.member.Member;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest //JPA 관련 빈만 로드하는 가벼운 테스트
class MemberRepositoryTest {

    @Autowired
    MemberRepository re;

    @Test
    void AfterSave_findByUsername_Check() {
        // given
        re.save(new Member("park", "encoded-password", "USER"));

        // when
        Optional<Member> foundUser = re.findByUsername("park");

        // then
        assertTrue(foundUser.isPresent());
        assertEquals("USER", foundUser.get().getRole());
        assertNotNull(foundUser.get().getId());

    }

    @Test
    void NullUsername_BeanResult() {
        assertTrue(re.findByUsername("nobody").isEmpty());
        assertFalse(re.existsByUsername("nobody"));

    }
}///