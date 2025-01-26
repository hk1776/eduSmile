package com.example.edusmile.Service;

import com.example.edusmile.Entity.MemberEntity;
import com.example.edusmile.Entity.Notice;
import com.example.edusmile.Entity.Subject;
import com.example.edusmile.Repository.NoticeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class NoticeService {
    private final NoticeRepository noticeRepository;
    private final SubjectService subjectService;
    private final MemberService memberService;

    public Notice save(String classId, String notice, Long memberId, String uuid) {
        Optional<Subject> subject = subjectService.findById(classId);
        Optional<MemberEntity> member = memberService.findById(memberId);
        String title = "["+subject.get().getSubject()+"] "+subject.get().getGrade()+"학년 "+subject.get().getDivClass()+"분반 공지사항";

        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String time = now.format(formatter);

        Notice input = new Notice();
        input.setTitle(title);
        input.setAuthor(member.get().getName());
        input.setViews(0);
        input.setNotice(notice);
        input.setCreated(time);
        input.setUpdated(time);
        input.setFile(uuid);
        input.setClassId(classId);
        return noticeRepository.save(input);
    }

    public Page<Notice> findByClassId(String classId, Pageable pageable) {
        return noticeRepository.findByClassId(classId, pageable);
    }

    public Notice findById(Long id) {
        return noticeRepository.findById(id).orElse(null);
    }
}
