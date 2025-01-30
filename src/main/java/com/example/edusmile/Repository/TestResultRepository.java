package com.example.edusmile.Repository;

import com.example.edusmile.Entity.Attend;
import com.example.edusmile.Entity.TestResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface TestResultRepository extends JpaRepository<TestResult,Long> {
    Optional<List<TestResult>> findByTestId(Long id);
}
