package com.example.edusmile.Repository;

import com.example.edusmile.Entity.Notice;
import com.example.edusmile.Entity.Summary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SummaryRepository extends JpaRepository<Summary, Long> {
    Page<Summary> findByClassId(String classId, Pageable pageable);

    void deleteByClassId(String classId);

    @Modifying
    @Query("UPDATE Summary f SET f.views = f.views + 1 WHERE f.id = :id")
    void increaseViews(@Param("id") Long id);
}
