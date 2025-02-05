package com.example.edusmile.Repository;

import com.example.edusmile.Entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    Page<Notice> findByClassId(String classId, Pageable pageable);
    void deleteByClassId(String classId);

    @Modifying
    @Query("UPDATE Notice f SET f.views = f.views + 1 WHERE f.id = :id")
    void increaseViews(@Param("id") Long id);
}
