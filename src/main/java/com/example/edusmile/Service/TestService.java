package com.example.edusmile.Service;

import com.example.edusmile.Entity.MemberEntity;
import com.example.edusmile.Entity.Notice;
import com.example.edusmile.Entity.Subject;
import com.example.edusmile.Entity.Test;
import com.example.edusmile.Repository.TestRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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

    public Test save(String classId, String exam, Long memberId) {
        Optional<Subject> subject = subjectService.findById(classId);
        Optional<MemberEntity> member = memberService.findById(memberId);
        String title = "["+subject.get().getSubject()+"] "+subject.get().getGrade()+"학년 "+subject.get().getDivClass()+"분반 시험";

        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String time = now.format(formatter);

        Test input = new Test();
        input.setTitle(title);
        input.setAuthor(member.get().getName());
        input.setViews(0);
        input.setExam(exam);
        input.setCreated(time);
        input.setUpdated(time);
        input.setClassId(classId);
        return testRepository.save(input);
    }
}
