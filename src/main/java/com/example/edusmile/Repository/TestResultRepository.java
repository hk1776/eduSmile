package com.example.edusmile.Repository;

import com.example.edusmile.Entity.Attend;
import com.example.edusmile.Entity.TestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface TestResultRepository extends JpaRepository<TestResult,Long> {
    Optional<List<TestResult>> findByTestId(Long id);

    @Modifying
    @Query("DELETE FROM TestResult e WHERE e.testId = :testId")
    void deleteByTestId(@Param("testId") long testId);
}
