package com.example.edusmile.Service;

import com.example.edusmile.Entity.Attend;
import com.example.edusmile.Entity.MemberEntity;
import com.example.edusmile.Entity.Subject;
import com.example.edusmile.Repository.AttendRepository;
import com.example.edusmile.Repository.SubjectRepository;
import jakarta.persistence.Column;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SubjectService {
    private final SubjectRepository subjectRepository;
    private final AttendService attendService;
    private final AttendRepository attendRepository;

    public List<Subject> teacherSubject(Long id) {
        List<Attend>attends = attendService.findByMemberId(id);
        ArrayList<Subject> subjects = new ArrayList<>();
        for (Attend attend : attends) {
            subjects.add(attend.getSubject());
        }
        return subjects;
    }

    public Optional<Subject> findById(String id) {
        return subjectRepository.findById(id);
    }
    public Subject save(MemberEntity member, String subjectInput, int grade, String divClass) {
        Subject subject = new Subject();
        subject.setSubject(subjectInput);
        subject.setAttends(new HashSet<>());
        subject.setGrade(grade);
        subject.setDivClass(divClass);

        String uuid = UUID.randomUUID().toString();
        String id = uuid.replace("-", "").substring(0, 5);
        subject.setId(id);
        Subject subjectEntity =  subjectRepository.save(subject);
        attendService.save(member,subjectEntity);
        return subjectEntity;
    }
    public void delete(String subjectId) {
        Subject subject = subjectRepository.findById(subjectId).orElseThrow(() -> new EntityNotFoundException("Subject not found"));
        attendRepository.deleteAttendBySubjectId(subjectId);
        subjectRepository.deleteSubjectById(subjectId);
    }

    public Subject update(String subjectId,String subjectInput,int grade,String divClass) {
        Optional<Subject> subject = subjectRepository.findById(subjectId);
        subject.get().setSubject(subjectInput);
        subject.get().setGrade(grade);
        subject.get().setDivClass(divClass);
        return subjectRepository.save(subject.get());
    }
}
