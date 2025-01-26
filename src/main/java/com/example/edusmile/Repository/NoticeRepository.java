package com.example.edusmile.Repository;

import com.example.edusmile.Entity.Attend;
import com.example.edusmile.Entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    Page<Notice> findByClassId(String classId, Pageable pageable);

}
