package com.example.db_project.repository;

import com.example.db_project.domain.Member;
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

    @Test
    @DisplayName("중복된 이메일은 저장할 수 없습니다")
    void 중복된_이메일은_저장할_수_없다() {
        memberRepository.saveAndFlush(new Member("회원1", "dup@test.com"));

        // save() 만 쓰면 아직 DB 로 안 나가서 안 터집니다. saveAndFlush() 로 바로 보냅니다.
        assertThatThrownBy(() ->
                memberRepository.saveAndFlush(new Member("회원2", "dup@test.com")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
