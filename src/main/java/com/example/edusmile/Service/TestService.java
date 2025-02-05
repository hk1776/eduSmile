package com.example.edusmile.Service;

import com.example.edusmile.Entity.*;
import com.example.edusmile.Repository.TestRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class TestService {
    private final TestRepository testRepository;
    private final SubjectService subjectService;
    private final MemberService memberService;
    private final TestResultService testResultService;

    public Test save(String classId, String exam,MemberEntity member) {
        Optional<Subject> subject = subjectService.findById(classId);
        String title = "["+subject.get().getSubject()+"] "+subject.get().getGrade()+"학년 "+subject.get().getDivClass()+"분반 시험";

        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String time = now.format(formatter);

        Test input = new Test();
        input.setTitle(title);
        input.setAuthor(member.getName());
        input.setMemberId(member.getId());
        input.setViews(0);
        input.setExam(exam);
        input.setCreated(time);
        input.setUpdated(time);
        input.setClassId(classId);
        return testRepository.save(input);
    }

    public Page<Test> findByClassId(String classId, Pageable pageable) {
        return testRepository.findByClassId(classId, pageable);
    }

    public Test findById(Long id) {
        Test test = testRepository.findById(id).orElse(null);
        testRepository.increaseViews(id);
        return test;
    }

    public void delete(Long id) {
        testRepository.deleteById(id);
        testResultService.deleteByTestId(id);
    }
    public void deleteByClassId(String classId) {
        testRepository.deleteByClassId(classId);
    }
}
