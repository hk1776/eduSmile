package com.example.edusmile.Repository;

import com.example.edusmile.Entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, String> {
    List<Subject> findByMemberId(Long id);
}
