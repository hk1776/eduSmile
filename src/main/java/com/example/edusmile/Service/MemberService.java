package com.example.edusmile.Service;

import com.example.edusmile.Dto.BoardDTO;
import com.example.edusmile.Entity.*;
import com.example.edusmile.Repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {
    private final MemberRepository memberRepository;
    private final AttendService attendService;

    public MemberEntity memberInfo(String loginId) {
        Optional<MemberEntity> member = memberRepository.findByloginId(loginId);
        return member.orElse(null);
    }
    public Optional<MemberEntity> findById(Long id) {
        return memberRepository.findById(id);
    }
    public void saveSubject(Long id, Subject subject) {
        Optional<MemberEntity> member = memberRepository.findById(id);
        attendService.save(member.get(), subject);
    }
    public void deleteOnlySubject(Long id, String subject) {
        attendService.memberSubjectDelete(id, subject);
    }
    public MemberEntity myTeacher(String code){
        List<MemberEntity> teacher = memberRepository.findByTeacherCodeTeacher(code,"teacher");
        return teacher.get(0);
    }


//    public void removeMemberFromSubject(Long memberId, String subjectId) {
//        // Member와 Subject 조회
//        MemberEntity member = memberRepository.findById(memberId)
//                .orElseThrow(() -> new IllegalArgumentException("Member not found"));
//        Subject subject = subjectRepository.findById(subjectId)
//                .orElseThrow(() -> new IllegalArgumentException("Subject not found"));
//
//        // Attend 객체 조회 및 삭제
//        Optional<Attend> attendToRemove = member.getAttends().stream()
//                .filter(attend -> attend.getSubject().equals(subject))
//                .findFirst();
//
//        attendToRemove.ifPresent(attend -> {
//            member.getAttends().remove(attend); // Member의 attends에서 제거
//            subject.removeAttend(attend);      // Subject의 attends에서 제거
//        });
//    }
}
