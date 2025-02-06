package com.example.edusmile.Service;

import com.example.edusmile.Entity.FreeBoard;
import com.example.edusmile.Entity.TestResult;
import com.example.edusmile.Repository.TestResultRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class TestResultService {
    private final TestResultRepository testResultRepository;

    public TestResult save(long testId,String classId, long memberId, String result, int score) {
        TestResult testResult = new TestResult();
        testResult.setTestId(testId);
        testResult.setClassId(classId);
        testResult.setMemberId(memberId);
        testResult.setResult(result);
        testResult.setScore(score);
        return testResultRepository.save(testResult);
    }

    public List<TestResult> findByTestId(long testId) {
        Optional<List<TestResult>> testResultsOptional = testResultRepository.findByTestId(testId);
        return testResultsOptional.orElse(Collections.emptyList());
    }

    public void deleteByTestId(long testId) {
        testResultRepository.deleteByTestId(testId);
    }

    public void deleteByClassId(String classId) {
        testResultRepository.deleteByClassId(classId);
    }

    public Page<TestResult> findByClassId(String classId, Pageable pageable){
        return testResultRepository.findByClassId(classId, pageable);
    }
}
