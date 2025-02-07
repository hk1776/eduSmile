package com.example.edusmile.Controller;

import com.example.edusmile.Dto.BoardDTO;
import com.example.edusmile.Entity.FreeBoard;
import com.example.edusmile.Entity.MemberEntity;
import com.example.edusmile.Entity.Subject;
import com.example.edusmile.Repository.MemberRepository;
import com.example.edusmile.Repository.SubjectRepository;
import com.example.edusmile.Service.FreeBoardService;
import com.example.edusmile.Service.MemberService;
import com.example.edusmile.Service.SubjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@RequiredArgsConstructor
@Slf4j
public class HomeController {
    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final SubjectService subjectService;
    private final FreeBoardService freeBoardService;

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
        List<FreeBoard> classBoard = freeBoardService.findByClassId(member.getTeacherCode());
        Collections.sort(classBoard, Comparator.comparing(FreeBoard::getId).reversed());
        List<FreeBoard> ClassBoard5 = classBoard.subList(0, Math.min(5, classBoard.size()));

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
        Optional<MemberEntity> m= memberRepository.findByloginId(user.getUsername());
        MemberEntity my = m.get();
        model.addAttribute("my", my);

        if(myTeacher != null) {
            model.addAttribute("teacherInfo", myTeacher);
            model.addAttribute("subjects", subjectsT);
            model.addAttribute("subLen", subjects.size());
            model.addAttribute("classBoard", ClassBoard5);
            model.addAttribute("teacher", member.getRole().equals("teacher"));
            model.addAttribute("isTeacher", member.getRole().equals("teacher"));

            log.info("member: {}", member.getName());
            return "main";
        }else {

            return "noClass";
        }
    }
    @PostMapping("/submitClass")
    public ResponseEntity<Map<String, String>> submitClassMember(@RequestParam String classId,
                                    @RequestParam Long memberId,
                                    RedirectAttributes redirectAttributes) {

        Map<String, String> response = new HashMap<>();
        if (classId == null || classId.trim().isEmpty()) {
            response.put("message", "반 코드가 비어 있습니다.");
            return ResponseEntity.badRequest().body(response);
        }

        MemberEntity teacher = memberService.myTeacher(classId);
        if (teacher == null) {
            response.put("message", "유효하지 않은 반 코드입니다.");
            return ResponseEntity.badRequest().body(response);
        }

        // 정상적인 처리 (예: 멤버 등록)
        memberService.setSchool(teacher, memberId);
        response.put("message", "반 코드가 성공적으로 등록되었습니다.");
        return ResponseEntity.ok(response);
    }
}
