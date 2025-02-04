package com.example.edusmile.Service;

import com.example.edusmile.Entity.FreeBoard;
import com.example.edusmile.Entity.Notice;
import com.example.edusmile.Repository.FreeBoardRepository;
import jakarta.persistence.Column;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class FreeBoardService {
    private final FreeBoardRepository freeBoardRepository;

    public FreeBoard save(String title,String author,Long memberId,int views, String content, String created, String updated, String file, String classId) {
        FreeBoard freeBoard = new FreeBoard();
        freeBoard.setTitle(title);
        freeBoard.setAuthor(author);
        freeBoard.setMemberId(memberId);
        freeBoard.setViews(views);
        freeBoard.setContent(content);
        freeBoard.setCreated(created);
        freeBoard.setUpdated(updated);
        freeBoard.setFile(file);
        freeBoard.setClassId(classId);
        return freeBoardRepository.save(freeBoard);
    }

    public Page<FreeBoard> findByClassId(String classId, Pageable pageable){
        return freeBoardRepository.findByClassId(classId, pageable);
    }

    public FreeBoard findById(Long id) {
        FreeBoard freeBoard = freeBoardRepository.findById(id).orElse(null);
        freeBoardRepository.increaseViews(id);
        return freeBoard;
    }

    public FreeBoard update(String title, String content, String uuid, Long id) {
        log.info("업데이트 uuid={}",uuid);
        FreeBoard free = freeBoardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 게시물을 찾을 수 없습니다: " + id));

        // 공지사항 정보 업데이트
        free.setTitle(title);
        free.setContent(content);
        free.setUpdated(LocalDate.now().toString());


        // 파일 업데이트 로직
        if (!uuid.equals("stay")) {
            String projectDir = Paths.get(System.getProperty("user.dir"), "file", "board", "free").toString();
            Path dirPath = Paths.get(projectDir);

            try (Stream<Path> files = Files.list(dirPath)) {
                String matchedFileName = files
                        .map(Path::getFileName)
                        .map(Path::toString)
                        .filter(name -> name.startsWith(free.getFile())) // 기존 파일명과 일치하는지 확인
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
                free.setFile("No");
            } else {
                free.setFile(uuid);
            }
        }

        return freeBoardRepository.save(free);
    }
    public void delete(Long id) {
        FreeBoard free = freeBoardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 게시물을 찾을 수 없습니다: " + id));

        String projectDir = Paths.get(System.getProperty("user.dir"), "file", "board", "free").toString();
        Path dirPath = Paths.get(projectDir);

        try (Stream<Path> files = Files.list(dirPath)) {
            String matchedFileName = files
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(name -> name.startsWith(free.getFile()))
                    .findFirst()
                    .orElse(null);

            if (matchedFileName != null) {
                Path oldFilePath = Paths.get(projectDir, matchedFileName);
                Files.deleteIfExists(oldFilePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        freeBoardRepository.deleteById(id);
    }
}
