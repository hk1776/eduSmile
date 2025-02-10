package com.example.edusmile.Service;

import com.example.edusmile.Entity.ClassLog;
import com.example.edusmile.Repository.ClassLogRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ClassLogService {

    private final ClassLogRepository classLogRepository;

    public ClassLog save(ClassLog classLog) {
        return classLogRepository.save(classLog);
    }
    public List<ClassLog> findAll() {
        return classLogRepository.findAll();
    }
}
