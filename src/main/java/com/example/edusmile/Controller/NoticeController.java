package com.example.edusmile.Controller;

import com.example.edusmile.Entity.MemberEntity;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/notice") // 공지사항 관련 경로
public class NoticeController {
    private final MemberService memberService;

    // 공지사항 목록 페이지
    @GetMapping
    public String getNoticeList(
            @AuthenticationPrincipal UserDetails user,
            Model model,
            @RequestParam(value = "page", defaultValue = "1") int page) {

        // 사용자 정보 확인
        MemberEntity member = memberService.memberInfo(user.getUsername());
        model.addAttribute("member", member);
        model.addAttribute("teacher", member.getRole().equals("teacher"));

        // 교사가 아닌 경우 메인 페이지로 이동
        if (!member.getRole().equals("teacher")) {
            return "main";
        }

        // 공지사항 예시 데이터
        List<Map<String, Object>> notices = new ArrayList<>(List.of(
                Map.of("id", 1, "title", "2017년도 교외체험학습 계획서 및 보고서", "author", "최**", "date", "2024-11-01", "views", 72),
                Map.of("id", 2, "title", "2018년도 입학생 3개년 교육과정 편제표", "author", "이**", "date", "2024-11-12", "views", 36),
                Map.of("id", 3, "title", "2학기 학부모님 대상 학교폭력 및 도박 예방 교육", "author", "김**", "date", "2024-11-14", "views", 17),
                Map.of("id", 4, "title", "2020년도 교외체험학습 계획서 및 보고서", "author", "최**", "date", "2024-11-17", "views", 43),
                Map.of("id", 5, "title", "2021년도 입학생 3개년 교육과정 편제표", "author", "이**", "date", "2024-11-24", "views", 19),
                Map.of("id", 6, "title", "2학기 학부모님 대상 학교폭력 및 도박 예방 교육", "author", "김**", "date", "2024-12-10", "views", 7),
                Map.of("id", 7, "title", "2022년도 교외체험학습 계획서 및 보고서", "author", "최**", "date", "2024-12-11", "views", 9),
                Map.of("id", 8, "title", "2023년도 입학생 3개년 교육과정 편제표", "author", "이**", "date", "2024-12-12", "views", 28),
                Map.of("id", 9, "title", "1학기 학부모님 대상 학교폭력 및 도박 예방 교육", "author", "김**", "date", "2024-12-24", "views", 17),
                Map.of("id", 10, "title", "2024년도 입학생 3개년 교육과정 편제표", "author", "김**", "date", "2025-01-07", "views", 48),
                Map.of("id", 10, "title", "3학기 학부모님 대상 학교폭력 및 도박 예방 교육", "author", "김**", "date", "2025-01-15", "views", 48),
                Map.of("id", 10, "title", "4학기 학부모님 대상 학교폭력 및 도박 예방 교육", "author", "김**", "date", "2025-01-21", "views", 48)
        ));

        // 날짜 형식 정의
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 최신순으로 정렬 (날짜 기반)
        notices.sort(Comparator.comparing((Map<String, Object> notice) ->
                LocalDate.parse((String) notice.get("date"), formatter)).reversed());

        // 페이지네이션 로직
        int itemsPerPage = 10; // 페이지당 표시할 글 개수
        int totalItems = notices.size(); // 전체 공지사항 개수
        int totalPages = (int) Math.ceil((double) totalItems / itemsPerPage); // 전체 페이지 수 계산

        // 현재 페이지에 해당하는 데이터 추출
        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, totalItems);
        List<Map<String, Object>> paginatedNotices = notices.subList(startIndex, endIndex);

        // 페이지네이션 데이터 생성
        List<Map<String, Object>> pages = new ArrayList<>();
        for (int i = 1; i <= totalPages; i++) {
            pages.add(Map.of(
                    "page", i,
                    "isActive", i == page // 현재 페이지 여부
            ));
        }

        // 모델에 데이터 추가
        model.addAttribute("notices", paginatedNotices);
        model.addAttribute("pages", pages);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("prevPage", page > 1 ? page - 1 : 1);
        model.addAttribute("nextPage", page < totalPages ? page + 1 : totalPages);

        return "notice"; // notice.mustache 템플릿
    }
}
