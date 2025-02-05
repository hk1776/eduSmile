package com.example.edusmile.Service;

import com.example.edusmile.Entity.MemberEntity;
import com.example.edusmile.Entity.Notice;
import com.example.edusmile.Entity.Subject;
import com.example.edusmile.Repository.NoticeRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class NoticeService {
    private final NoticeRepository noticeRepository;
    private final SubjectService subjectService;
    private final MemberService memberService;

    public Notice save(String title,String classId, String notice,MemberEntity member, String uuid) {
        Optional<Subject> subject = subjectService.findById(classId);
        if(title.equals("AI")){
            title = "["+subject.get().getSubject()+"] "+subject.get().getGrade()+"학년 "+subject.get().getDivClass()+"분반 공지사항";
        }

        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String time = now.format(formatter);

        Notice input = new Notice();
        input.setTitle(title);
        input.setAuthor(member.getName());
        input.setMemberId(member.getId());
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
        log.info("조회수 증가");
        Notice notice = noticeRepository.findById(id).orElse(null);
        noticeRepository.increaseViews(id);
        return notice;
    }

    public Notice update(String title, String content, String uuid, Long id) {
        log.info("업데이트 id={}",id);
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 게시물을 찾을 수 없습니다: " + id));

        // 공지사항 정보 업데이트
        notice.setTitle(title);
        notice.setNotice(content);
        notice.setUpdated(LocalDate.now().toString());

        // 파일 업데이트 로직
        if (!uuid.equals("stay")) {
            String projectDir = Paths.get(System.getProperty("user.dir"), "file", "board", "notice").toString();
            Path dirPath = Paths.get(projectDir);

            try (Stream<Path> files = Files.list(dirPath)) {
                String matchedFileName = files
                        .map(Path::getFileName)
                        .map(Path::toString)
                        .filter(name -> name.startsWith(notice.getFile())) // 기존 파일명과 일치하는지 확인
                        .findFirst()
                        .orElse(null);

                if (matchedFileName != null) {
                    Path oldFilePath = Paths.get(projectDir, matchedFileName);
                    Files.deleteIfExists(oldFilePath); // 기존 파일 삭제
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (uuid.equals("delete")) {
                notice.setFile("No");
            } else {
                notice.setFile(uuid);
            }
        }

        return noticeRepository.save(notice);
    }
    public void deleteNotice(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 게시물을 찾을 수 없습니다: " + id));

        String projectDir = Paths.get(System.getProperty("user.dir"), "file", "board", "notice").toString();
        Path dirPath = Paths.get(projectDir);

        try (Stream<Path> files = Files.list(dirPath)) {
            String matchedFileName = files
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(name -> name.startsWith(notice.getFile())) // 기존 파일명과 일치하는지 확인
                    .findFirst()
                    .orElse(null);

            if (matchedFileName != null) {
                Path oldFilePath = Paths.get(projectDir, matchedFileName);
                Files.deleteIfExists(oldFilePath); // 기존 파일 삭제
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        noticeRepository.deleteById(id);
    }

    public void deleteByClassId(String classId) {
        noticeRepository.deleteByClassId(classId);
    }
}
