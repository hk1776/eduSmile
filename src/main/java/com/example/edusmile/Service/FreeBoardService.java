package com.example.edusmile.Service;

import com.example.edusmile.Entity.FreeBoard;
import com.example.edusmile.Entity.Notice;
import com.example.edusmile.Repository.FreeBoardRepository;
import jakarta.persistence.Column;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class FreeBoardService {
    private final FreeBoardRepository freeBoardRepository;

    public FreeBoard save(String title,String author, int views, String content, String created, String updated, String file, String classId) {
        FreeBoard freeBoard = new FreeBoard();
        freeBoard.setTitle(title);
        freeBoard.setAuthor(author);
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
        return freeBoardRepository.findById(id).orElse(null);
    }
}
