package com.example.edusmile.Repository;

import com.example.edusmile.Entity.CounselEntity;
import com.example.edusmile.Entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CounselRepository extends JpaRepository<CounselEntity,Long> {

    List<CounselEntity> findByClassId(String teacherCode);


    @Query("SELECT c FROM CounselEntity c WHERE c.classId=:teacherCode ORDER BY c.created DESC")
    List<CounselEntity> findByTeacherCodeDesc(String teacherCode);


    @Query(value = "select c from CounselEntity c " +
            "where c.loginId =:loginId AND c.type=:type")
    List<CounselEntity> duplicateContent_loginId(@Param("loginId") String loginId, @Param("type") String type );

    @Query(value = "select c from CounselEntity c " +
            "where c.loginId =:loginId AND c.type=:type")
    List<CounselEntity> findCounsel(@Param("loginId") String loginId, @Param("type") String type );
}
