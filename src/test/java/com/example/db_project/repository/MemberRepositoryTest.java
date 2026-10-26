package com.example.db_project.repository;

import com.example.db_project.domain.Member;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MemberRepositoryTest {

    @Autowired MemberRepository memberRepository;
    @PersistenceContext EntityManager em;

    @Test
    @DisplayName("중복된 이메일은 저장할 수 없습니다")
    void 중복된_이메일은_저장할_수_없다() {
        memberRepository.save(new Member("회원1", "dup@test.com"));

        assertThatThrownBy(() -> {
            memberRepository.save(new Member("회원2", "dup@test.com"));
            em.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
}
