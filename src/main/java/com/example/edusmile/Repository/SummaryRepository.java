package com.example.edusmile.Repository;

import com.example.edusmile.Entity.Notice;
import com.example.edusmile.Entity.Summary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SummaryRepository extends JpaRepository<Summary, Long> {
    Page<Summary> findByClassId(String classId, Pageable pageable);

}
