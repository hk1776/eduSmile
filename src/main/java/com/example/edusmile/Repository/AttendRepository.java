package com.example.edusmile.Repository;

import com.example.edusmile.Entity.Attend;
import com.example.edusmile.Entity.MemberEntity;
import com.example.edusmile.Entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AttendRepository extends JpaRepository<Attend,Long> {

    List<Attend> findByMember_Id(Long id);

    List<Attend> findBySubject_Id(String id);

    @Modifying
    @Transactional
    @Query("DELETE FROM Attend a WHERE a.subject.id = :subjectId")
    void deleteAttendBySubjectId(@Param("subjectId") String subjectId);
}
