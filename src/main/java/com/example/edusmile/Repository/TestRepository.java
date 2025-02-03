package com.example.edusmile.Repository;

import com.example.edusmile.Entity.Notice;
import com.example.edusmile.Entity.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TestRepository extends JpaRepository<Test, Long> {
    Page<Test> findByClassId(String classId, Pageable pageable);

    @Modifying
    @Query("UPDATE Test f SET f.views = f.views + 1 WHERE f.id = :id")
    void increaseViews(@Param("id") Long id);
}
