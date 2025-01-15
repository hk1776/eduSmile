package com.example.edusmile.Repository;

import com.example.edusmile.Entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Member;
import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, Integer> {
        Optional<MemberEntity> findByloginId(String loginId);

        @Query(value = "select member from MemberEntity member " +
                "where member.teacherCode =:teacherCode and member.role= :role ")
        List<MemberEntity> findByTeacherCodeTeacher(@Param("teacherCode") String teacherCode , @Param("role") String role);

        @Query(value = "select member from MemberEntity member " +
                "where member.name =:name and member.phoneNumber= :phoneNumber ")
        List<MemberEntity> findByNameAndPhoneNumber(@Param("name") String name , @Param("phoneNumber") String phoneNumber );
}
