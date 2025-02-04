package com.example.edusmile.Controller;

import com.example.edusmile.Dto.BoardDTO;
import com.example.edusmile.Entity.MemberEntity;
import com.example.edusmile.Entity.Subject;
import com.example.edusmile.Repository.MemberRepository;
import com.example.edusmile.Service.MemberService;
import com.example.edusmile.Service.SubjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@Slf4j
public class HomeController {
    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final SubjectService subjectService;

    @GetMapping("/")
    public String index(Model model) {
        return "index";
    }

    @GetMapping("/home")
    public String home(@AuthenticationPrincipal UserDetails user, Model model) {

        log.info("user = {}",user.getUsername());
        MemberEntity member  = memberService.memberInfo(user.getUsername());
        MemberEntity myTeacher = memberService.myTeacher(member.getTeacherCode());
        List<Subject> subjects = subjectService.getMemberSubject(member.getId());
        subjects.sort(Comparator
                .comparing(Subject::getGrade) // 이름 기준 오름차순
                .thenComparing(Subject::getDivClass));
        List<BoardDTO.subjectTeacherName> subjectsT = new ArrayList<>();
        for(Subject i : subjects) {
            BoardDTO.subjectTeacherName subjectT = new BoardDTO.subjectTeacherName();
            subjectT.setId(i.getId());
            subjectT.setSubject(i.getSubject());
            subjectT.setGrade(i.getGrade());
            subjectT.setDivClass(i.getDivClass());
            Optional<MemberEntity> teacher = memberService.findById(i.getTeacherId());
            subjectT.setTeacher(teacher.get());
            subjectsT.add(subjectT);
        }


        model.addAttribute("member", member);
        model.addAttribute("subjects", subjectsT);
        model.addAttribute("subLen",subjects.size());
        model.addAttribute("teacherInfo", myTeacher);
        model.addAttribute("teacher",member.getRole().equals("teacher"));

        //헤더 있는페이지는 이거 필수
        List<MemberEntity> listteacher = memberRepository.findByTeacherCodeTeacher(member.getTeacherCode(),"teacher");
        MemberEntity teacher = listteacher.get(0);
        model.addAttribute("class-teacher", teacher);
        //여기 까지

        log.info("member: {}", member.getName());
        return "main";
    }
}
