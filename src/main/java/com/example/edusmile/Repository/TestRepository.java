package com.example.edusmile.Repository;

import com.example.edusmile.Entity.Notice;
import com.example.edusmile.Entity.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestRepository extends JpaRepository<Test, Long> {
    Page<Test> findByClassId(String classId, Pageable pageable);
}
