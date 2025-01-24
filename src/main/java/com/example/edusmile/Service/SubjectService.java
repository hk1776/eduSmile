package com.example.edusmile.Service;

import com.example.edusmile.Entity.Subject;
import com.example.edusmile.Repository.SubjectRepository;
import jakarta.persistence.Column;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubjectService {
    private final SubjectRepository subjectRepository;
    private final MemberService memberService;

    public List<Subject> teacherSubject(Long id) {
        return subjectRepository.findByMemberId(id);
    }
    public Optional<Subject> findById(String id) {
        return subjectRepository.findById(id);
    }
    public Subject save(Long memberId, String subjectInput,int grade,String divClass) {
        Subject subject = new Subject();
        subject.setSubject(subjectInput);
        subject.setMemberId(memberId);
        subject.setGrade(grade);
        subject.setDivClass(divClass);
        String uuid = UUID.randomUUID().toString();
        String id = uuid.replace("-", "").substring(0, 5);
        subject.setId(id);
        memberService.saveSubject(memberId,id);
        return subjectRepository.save(subject);
    }

    public void delete(Long memberId, String subjectId) {
        subjectRepository.deleteById(subjectId);
        memberService.deleteSubject(memberId,subjectId);
    }

    public Subject update(String subjectId,String subjectInput,int grade,String divClass) {
        Optional<Subject> subject = subjectRepository.findById(subjectId);
        subject.get().setSubject(subjectInput);
        subject.get().setGrade(grade);
        subject.get().setDivClass(divClass);
        return subjectRepository.save(subject.get());
    }
}
