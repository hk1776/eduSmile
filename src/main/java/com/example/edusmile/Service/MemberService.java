package com.example.edusmile.Service;

import com.example.edusmile.Dto.BoardDTO;
import com.example.edusmile.Entity.*;
import com.example.edusmile.Repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {
    private final MemberRepository memberRepository;
    private final AttendService attendService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

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
        if(!teacher.isEmpty()){
            return teacher.get(0);
        }else {
            return null;
        }
    }

    public List<MemberEntity> myStudent(String code){
        return memberRepository.findByTeacherCodeTeacher(code,"student");
    }

    public void studentClassDel(Long id){
        Optional<MemberEntity> member = memberRepository.findById(id);
        member.get().setTeacherCode("&");
        member.get().setSchoolClass(-1);
        member.get().setSchoolgrade(-1);
        memberRepository.save(member.get());
    }

    public void setSchool(MemberEntity teacher, long student){
        Optional<MemberEntity> member = findById(student);
        member.get().setTeacherCode(teacher.getTeacherCode());
        member.get().setSchoolClass(teacher.getSchoolClass());
        member.get().setSchoolgrade(teacher.getSchoolgrade());
        memberRepository.save(member.get());
    }

    public void changeClass (long id,String school,int schoolgrade,int schoolClass){
        Optional<MemberEntity> teacher = memberRepository.findById(id);

        teacher.get().setSchool(school);
        teacher.get().setSchoolgrade(schoolgrade);
        teacher.get().setSchoolClass(schoolClass);
        memberRepository.save(teacher.get());
        List<MemberEntity> student = memberRepository.findByTeacherCodeTeacher(teacher.get().getTeacherCode(), "student");
        for(MemberEntity member : student){
            member.setSchool(school);
            member.setSchoolgrade(schoolgrade);
            member.setSchoolClass(schoolClass);
            memberRepository.save(member);
        }

    }

    public int memberCnt(){
        return memberRepository.findAll().size();
    }

    public int schoolCnt(){
        HashSet<String> schools = new HashSet<>();
        memberRepository.findAll()
                .forEach(member -> schools.add(member.getSchool()));
        return schools.size();
    }
    public int teacherCnt(){
        HashSet<String> teachers = new HashSet<>();
        memberRepository.findAll()
                .forEach(member -> teachers.add(member.getTeacherCode()));

        return teachers.size();
    }
    public int studentCnt(){
        HashSet<Long> students = new HashSet<>();
        List<MemberEntity> all = memberRepository.findAll();
        for(MemberEntity member : all){
            if(member.getRole().equals("student")){
                students.add(member.getId());
            }
        }
        return students.size();
    }

    public int adminCnt(){
        HashSet<Long> students = new HashSet<>();
        List<MemberEntity> all = memberRepository.findAll();
        for(MemberEntity member : all){
            if(member.getRole().equals("ADMIN")){
                students.add(member.getId());
            }
        }
        return students.size();
    }

    public boolean findByLoginId(String loginId) {

        if(memberRepository.findByloginId(loginId).isEmpty())
            return false;
        else
            return true;
    }


    public void saveAdmin(String adminId , String admimPassword,String Role)
    {
        MemberEntity admin = new MemberEntity();
        admin.setLoginId(adminId);
        admin.setRole("ADMIN");
        admin.setName("ADMIN");
        admin.setPhoneNumber(9999999999L);
        admin.setSchool("ADMIN");
        admin.setSchoolgrade(-1);
        admin.setSchoolClass(-1);
        admin.setImg_path("1234");
        admin.setPassword(encoder.encode(admimPassword));

        memberRepository.save(admin);
    }

    public List<MemberEntity> findAll(){
        return memberRepository.findAll();
    }

    public void updateUserRole(Long memberId, String newRole) {
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        member.setRole(newRole);
        memberRepository.save(member);
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
