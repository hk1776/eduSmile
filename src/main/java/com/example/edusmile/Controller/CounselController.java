package com.example.edusmile.Controller;


import com.example.edusmile.Entity.CounselEntity;
import com.example.edusmile.Entity.MemberEntity;
import com.example.edusmile.Repository.CounselRepository;
import com.example.edusmile.Repository.MemberRepository;
import com.example.edusmile.Service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.lang.reflect.Member;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Controller
@Slf4j
@RequiredArgsConstructor
public class CounselController {

    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final CounselRepository counselRepository;

    @GetMapping("/teacher/student_recode")
    public String student_recode(@AuthenticationPrincipal UserDetails user, Model model) {
        MemberEntity member  = memberService.memberInfo(user.getUsername());
        model.addAttribute("member", member);
        model.addAttribute("teacher",member.getRole().equals("teacher"));

        //헤더 있는페이지는 이거 필수
        List<MemberEntity> listteacher = memberRepository.findByTeacherCodeTeacher(member.getTeacherCode(),"teacher");
        MemberEntity teacher = listteacher.get(0);
        model.addAttribute("class-teacher", teacher);
        //여기 까지

        List<MemberEntity> students = memberRepository.findByRoleAndTeacherCode("student",member.getTeacherCode());  //자기반학생찾기
        model.addAttribute("students", students);
        if(!member.getRole().equals("teacher")) {
            return "main";
        }else{
            return "student_recode";
        }
    }

    @GetMapping("/teacher/student_recode/{id}")
    public String student_recode_detail(@AuthenticationPrincipal UserDetails user,@PathVariable Long    id, Model model) {



        MemberEntity member  = memberService.memberInfo(user.getUsername());
        MemberEntity student = memberRepository.findById(id).orElse(null);
        model.addAttribute("member", member);
        model.addAttribute("student", student);
        model.addAttribute("teacher",member.getRole().equals("teacher"));

        //헤더 있는페이지는 이거 필수
        List<MemberEntity> listteacher = memberRepository.findByTeacherCodeTeacher(member.getTeacherCode(),"teacher");
        MemberEntity teacher = listteacher.get(0);
        model.addAttribute("class-teacher", teacher);
        //여기 까지

        List<MemberEntity> students = memberRepository.findByRoleAndTeacherCode("student",member.getTeacherCode());  //자기반학생찾기
        model.addAttribute("students", students);
        if(!member.getRole().equals("teacher")) {
            return "main";
        }else{
            return "student_recode_detail";
        }
    }


    // 상담내역
    @GetMapping("/CounselList")
    public String getCounselList(
            @AuthenticationPrincipal UserDetails user,
            Model model,
            @RequestParam(value = "page", defaultValue = "1") int page) {




        // 사용자 정보 확인
        MemberEntity member = memberService.memberInfo(user.getUsername());
        model.addAttribute("member", member);
        model.addAttribute("teacher", member.getRole().equals("teacher"));


        List<MemberEntity> students = memberRepository.findByRoleAndTeacherCode("student",member.getTeacherCode());
        model.addAttribute("my_students", students);

        //헤더 있는페이지는 이거 필수
        List<MemberEntity> listteacher = memberRepository.findByTeacherCodeTeacher(member.getTeacherCode(),"teacher");
        MemberEntity teacher = listteacher.get(0);
        model.addAttribute("class-teacher", teacher);
        //여기 까지

        String teacherCode = teacher.getTeacherCode();

        List<CounselEntity> counsels = counselRepository.findByTeacherCodeDesc(teacherCode);


        // 페이지네이션 로직
        int itemsPerPage = 10; // 페이지당 표시할 글 개수
        int totalItems = counsels.size(); // 전체 공지사항 개수
        int totalPages = (int) Math.ceil((double) totalItems / itemsPerPage); // 전체 페이지 수 계산

        // 현재 페이지에 해당하는 데이터 추출
        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, totalItems);
        //List<Map<String, Object>> paginatedNotices = counsels.subList(startIndex, endIndex);

        // 페이지네이션 데이터 생성
        List<Map<String, Object>> pages = new ArrayList<>();
        for (int i = 1; i <= totalPages; i++) {
            pages.add(Map.of(
                    "page", i,
                    "isActive", i == page // 현재 페이지 여부
            ));
        }




        // 모델에 데이터 추가
        model.addAttribute("counsels",counsels);
        model.addAttribute("pages", pages);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("prevPage", page > 1 ? page - 1 : 1);
        model.addAttribute("nextPage", page < totalPages ? page + 1 : totalPages);

        return "Counsel";
    }


}
