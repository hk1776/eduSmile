package com.example.edusmile.Service;

import com.example.edusmile.Entity.MemberEntity;
import com.example.edusmile.Entity.Notice;
import com.example.edusmile.Entity.Subject;
import com.example.edusmile.Entity.Summary;
import com.example.edusmile.Repository.NoticeRepository;
import com.example.edusmile.Repository.SummaryRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional
public class SummaryService {
    private final SummaryRepository summaryRepository;
    private final SubjectService subjectService;
    private final MemberService memberService;

    public Summary save(String classId, String summary, MemberEntity member) {
        Optional<Subject> subject = subjectService.findById(classId);
        String title = "["+subject.get().getSubject()+"] "+subject.get().getGrade()+"학년 "+subject.get().getDivClass()+"분반 수업요약";

        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String time = now.format(formatter);

        String file = "NO";

        Summary input = new Summary();
        input.setTitle(title);
        input.setAuthor(member.getName());
        input.setMemberId(member.getId());
        input.setViews(0);
        input.setSummary(summary);
        input.setCreated(time);
        input.setUpdated(time);
        input.setClassId(classId);
        return summaryRepository.save(input);
    }

    public Page<Summary> findByClassId(String classId, Pageable pageable) {
        return summaryRepository.findByClassId(classId, pageable);
    }

    public Summary findById(Long id) {
        Summary summary = summaryRepository.findById(id).orElse(null);
        summaryRepository.increaseViews(id);
        return summary;
    }

    public Summary update(Long id, String title, String content) {
        Summary summary = summaryRepository.findById(id).orElse(null);
        summary.setTitle(title);
        summary.setSummary(content);
        summary.setUpdated(LocalDate.now().toString());
        return summaryRepository.save(summary);
    }

    public void delete(Long id) {
        summaryRepository.deleteById(id);
    }

    public void deleteByClassId(String classId) {
        summaryRepository.deleteByClassId(classId);
    }
}
