package com.example.edusmile.Repository;

import com.example.edusmile.Entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, Integer> {
        Optional<MemberEntity> findByloginId(String loginId);

}
