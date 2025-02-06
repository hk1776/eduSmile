package com.example.edusmile.Service;

import com.example.edusmile.Entity.Attend;
import com.example.edusmile.Entity.MemberEntity;
import com.example.edusmile.Entity.Subject;
import com.example.edusmile.Repository.AttendRepository;
import com.example.edusmile.Repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AttendService {
    private final AttendRepository repository;
    private final AttendRepository attendRepository;
    private final SubjectRepository subjectRepository;
    public Attend save(MemberEntity member, Subject subject) {
        Attend attend = new Attend(member, subject);
        return repository.save(attend);
    }

    public void memberDelete(Long memberId) {
        List<Attend> attends = repository.findAll(); // 모든 Attend 데이터를 가져옴
        for (Attend attend : attends) {
            if (attend.getMember().getId().equals(memberId)) {
                repository.delete(attend);  // 조건이 맞으면 삭제
            }
        }
    }

    public void subjectDelete(String subjectId) {
        List<Attend> attends = repository.findAll(); // 모든 Attend 데이터를 가져옴
        for (Attend attend : attends) {
            if (attend.getSubject().getId().equals(subjectId)) {
                repository.delete(attend);  // 조건이 맞으면 삭제
            }
        }
    }

    public void memberSubjectDelete(Long memberId, String subjectId) {
        List<Attend> attends = repository.findAll(); // 모든 Attend 데이터를 가져옴
        for (Attend attend : attends) {
            if (attend.getSubject().getId().equals(subjectId)&&attend.getMember().getId().equals(memberId)) {
                repository.delete(attend);  // 조건이 맞으면 삭제
            }
        }
    }

    public List<Attend> findByMemberId(Long memberId) {
        return repository.findByMember_Id(memberId);
    }

    public List<Attend> findBySubjectId(String subjectId) {
        return repository.findBySubject_Id(subjectId);
    }
    public void delete(Attend attend) {
        repository.delete(attend);
    }

    public void deleteBySubject(String subjectId) {
        List<Attend> attends = repository.findBySubject_Id(subjectId);
        repository.deleteAll(attends);
    }

public void delete_subject(String subjectId) {
    Optional<Subject> sub = subjectRepository.findById(subjectId);

    if(sub.isPresent()) {
        Subject subject = sub.get();
        subjectRepository.deleteSubjectById(subject.getId());

    }

}

}
