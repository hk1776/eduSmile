package com.example.edusmile.Repository;

import com.example.edusmile.Entity.Visitor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface VisitorRepository extends JpaRepository<Visitor, LocalDate> {
    Optional<Visitor> findByDate(LocalDate date);

    // 최근 7일간 방문자 수 통계
    @Query("SELECT v.date, v.count FROM Visitor v WHERE v.date BETWEEN :startDate AND :endDate ORDER BY v.date ASC")
    List<Object[]> findWeeklyVisitorStats(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
