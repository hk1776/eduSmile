package com.example.edusmile.Repository;

import com.example.edusmile.Entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, String> {
    @Modifying
    @Transactional
    @Query("DELETE FROM Subject s WHERE s.id = :id")
    void deleteSubjectById(@Param("id") String id);


    @Query("SELECT s FROM Subject s where s.teacherId = :teacherId")
    List<Subject> findSubjectByTeacherId(@Param("teacherId") Long teacherId);
}
