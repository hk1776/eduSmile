package com.example.edusmile.Service;

import com.example.edusmile.Entity.Subject;
import com.example.edusmile.Repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
@RequiredArgsConstructor
@Slf4j
public class SubjectService {
    private final SubjectRepository subjectRepository;

    public List<Subject> teacherSubject(Long id) {
        return subjectRepository.findByMemberId(id);
    }
    public Optional<Subject> findById(String id) {
        return subjectRepository.findById(id);
    }
}
