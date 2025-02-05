package com.example.edusmile.Repository;

import com.example.edusmile.Entity.FreeBoard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FreeBoardRepository extends JpaRepository<FreeBoard, Long> {
    Page<FreeBoard> findByClassId(String classId, Pageable pageable);
    List<FreeBoard> findByClassId(String classId);
    void deleteByClassId(String classId);

    @Modifying
    @Query("UPDATE FreeBoard f SET f.views = f.views + 1 WHERE f.id = :id")
    void increaseViews(@Param("id") Long id);
}
