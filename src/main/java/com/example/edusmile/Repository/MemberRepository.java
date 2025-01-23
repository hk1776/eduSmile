package com.example.edusmile.Repository;

import com.example.edusmile.Entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Member;
import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, Long> {
        Optional<MemberEntity> findByloginId(String loginId);

        @Query(value = "select member from MemberEntity member " +
                "where member.teacherCode =:teacherCode and member.role= :role ")
        List<MemberEntity> findByTeacherCodeTeacher(@Param("teacherCode") String teacherCode , @Param("role") String role);

        @Query(value = "select member from MemberEntity member " +
                "where member.name =:name and member.phoneNumber= :phoneNumber ")
        List<MemberEntity> findByNameAndPhoneNumber(@Param("name") String name , @Param("phoneNumber") String phoneNumber );

        @Query(value = "select member from MemberEntity member " +
                "where member.loginId = :LoginId and member.name =:name and member.phoneNumber= :phoneNumber ")
        List<MemberEntity> findByIDAndNameAndPhoneNumber(@Param("LoginId") String LoginId,@Param("name") String name , @Param("phoneNumber") String phoneNumber );

        @Query(value = "select member from MemberEntity member " +
                "where member.role = :role and member.teacherCode =:teacherCode")
        List<MemberEntity> findByRoleAndTeacherCode(@Param("role") String Role,@Param("teacherCode") String TeacherCode);

        @Modifying          //update, delete 시 사용
        @Transactional      // 원자성 유지
        @Query(value = "update MemberEntity member set member.password = :new_password " +
                "where member.loginId = :LoginId")
        void reset_password(@Param("LoginId") String LoginId, @Param("new_password") String new_password);


}
