package com.example.edusmile.Controller;

import com.example.edusmile.Dto.BoardDTO;
import com.example.edusmile.Dto.ClassDTO;
import com.example.edusmile.Entity.Attend;
import com.example.edusmile.Entity.FreeBoard;
import com.example.edusmile.Entity.MemberEntity;
import com.example.edusmile.Entity.Subject;
import com.example.edusmile.Repository.AttendRepository;
import com.example.edusmile.Repository.MemberRepository;
import com.example.edusmile.Service.AttendService;
import com.example.edusmile.Service.FreeBoardService;
import com.example.edusmile.Service.MemberService;
import com.example.edusmile.Service.SubjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/myClass")
public class MyClassController {
    private final MemberService memberService;
    private final FreeBoardService freeBoardService;
    private final MemberRepository memberRepository;
    private final SubjectService subjectService;
    private final AttendRepository attendRepository;
    private final AttendService attendService;

    @GetMapping("/list")
    public String myClassList(@RequestParam("id") String subjectId,
                              @RequestParam(value = "page", defaultValue = "1") int page,
                              Model model,
                              @AuthenticationPrincipal UserDetails user) {
        MemberEntity member  = memberService.memberInfo(user.getUsername());
        Pageable pageable = PageRequest.of(page - 1, 10, Sort.by(Sort.Order.desc("id")));

        Page<FreeBoard> freePage  = freeBoardService.findByClassId(subjectId, pageable);



        //헤더 있는페이지는 이거 필수
        List<MemberEntity> listteacher = memberRepository.findByTeacherCodeTeacher(member.getTeacherCode(),"teacher");
        MemberEntity teacher = listteacher.get(0);
        model.addAttribute("class-teacher", teacher);
        //여기 까지


        // 페이지 번호 리스트 계산
        List<Integer> pageNums = new ArrayList<>();
        for (int i = 1; i <= freePage.getTotalPages(); i++) {
            pageNums.add(i);
        }
        Integer prevPageNum = (freePage.hasPrevious()) ? page - 1 : null;
        Integer nextPageNum = (freePage.hasNext()) ? page + 1 : null;

        model.addAttribute("member", member);
        model.addAttribute("subjectId", subjectId);
        model.addAttribute("freePage", freePage);
        model.addAttribute("pageNums", pageNums);
        model.addAttribute("prevPageNum", prevPageNum);
        model.addAttribute("nextPageNum", nextPageNum);
        return "myClassList";
    }

    @GetMapping
    public String myClass(@RequestParam("id") String subjectId,
                          @RequestParam("num") Long id,
                          Model model,
                          @AuthenticationPrincipal UserDetails user) {
        MemberEntity member  = memberService.memberInfo(user.getUsername());

        FreeBoard free = freeBoardService.findById(id);

        String uuid = free.getFile(); // 저장된 UUID 값

        // 파일이 저장된 디렉토리
        String projectDir = Paths.get(System.getProperty("user.dir"), "file", "board", "free").toString();
        Path dirPath = Paths.get(projectDir);

        //헤더 있는페이지는 이거 필수
        List<MemberEntity> listteacher = memberRepository.findByTeacherCodeTeacher(member.getTeacherCode(),"teacher");
        MemberEntity teacher = listteacher.get(0);
        model.addAttribute("class-teacher", teacher);
        //여기 까지

        // 디렉토리가 존재하지 않으면 생성
        if (!Files.exists(dirPath)) {
            try {
                Files.createDirectories(dirPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String matchedFileName = null;
        boolean fileExists = false;

        // 디렉토리 내의 파일 목록을 검색
        try (Stream<Path> files = Files.list(dirPath)) {
            matchedFileName = files
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(name -> name.startsWith(uuid))
                    .findFirst()
                    .orElse(null);

            fileExists = (matchedFileName != null); // 파일 존재 여부 확인
        } catch (IOException e) {
            e.printStackTrace();
        }
        String filename = "";
        if(fileExists) {
            filename = matchedFileName.substring(36);
        }

        log.info("filename = {}", filename);
        model.addAttribute("fileExists", fileExists);
        model.addAttribute("author", Objects.equals(free.getMemberId(), member.getId()));
        model.addAttribute("filename", filename);
        model.addAttribute("originFilename", matchedFileName);
        model.addAttribute("subjectId", subjectId);
        model.addAttribute("member", member);
        model.addAttribute("free", free);
        return "myClassDetail";
    }

    @GetMapping("/write")
    public String myClassSaveForm(@RequestParam("id") String subjectId,
                                  Model model,
                                  @AuthenticationPrincipal UserDetails user) {
        MemberEntity member  = memberService.memberInfo(user.getUsername());
        model.addAttribute("subjectId", subjectId);
        model.addAttribute("today", LocalDate.now().toString());

        model.addAttribute("member", member);

        //헤더 있는페이지는 이거 필수
        List<MemberEntity> listteacher = memberRepository.findByTeacherCodeTeacher(member.getTeacherCode(),"teacher");
        MemberEntity teacher = listteacher.get(0);
        model.addAttribute("class-teacher", teacher);
        //여기 까지

        return "myClassNew";
    }
    @PostMapping("/write")
    public String myClassSave(Model model,
                              @AuthenticationPrincipal UserDetails user,
                              @ModelAttribute BoardDTO.Free free,
                              @RequestParam("file") MultipartFile file) {

        MemberEntity member  = memberService.memberInfo(user.getUsername());

        boolean fileCheck = false;
        if (file != null && !file.isEmpty()) {
            fileCheck = true;
        }
        UUID uuid = UUID.randomUUID();
        FreeBoard save =null;
        if(fileCheck){
            save = freeBoardService.save(free.getTitle(), free.getAuthor(),member.getId(),0,free.getContent(),free.getCreatedAt(),free.getCreatedAt(),uuid.toString(),free.getClassId());
        }else{
            save = freeBoardService.save(free.getTitle(), free.getAuthor(),member.getId(),0,free.getContent(),free.getCreatedAt(),free.getCreatedAt(),"No",free.getClassId());
        }

        String projectDir = Paths.get(System.getProperty("user.dir"), "file", "board","free").toString();
        File directory = new File(projectDir);
        if (!directory.exists()) {
            boolean created = directory.mkdirs(); // 폴더 생성
            if (created) {
                System.out.println("디렉토리 생성 성공: " + projectDir);
            } else {
                System.err.println("디렉토리 생성 실패!");
            }
        } else {
            System.out.println("디렉토리가 이미 존재합니다: " + projectDir);
        }

        if(fileCheck) {
            try {
                String originalFilename = file.getOriginalFilename();;
                String savePath = Paths.get(projectDir, uuid+originalFilename).toString();
                file.transferTo(new File(savePath));
                System.out.println("파일 저장 완료: " + savePath);
            } catch (IOException e) {
                e.printStackTrace();
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드 실패");
            }
        }

        return "redirect:/myClass?id=" + free.getClassId() + "&num=" + save.getId();
    }

    @GetMapping("/update")
    public String freeUpdateForm(@RequestParam("id") String subjectId,
                                 @RequestParam("num") Long id,
                                 Model model,
                                 @AuthenticationPrincipal UserDetails user) {
        MemberEntity member  = memberService.memberInfo(user.getUsername());
        FreeBoard free = freeBoardService.findById(id);

        String uuid = free.getFile(); // 저장된 UUID 값

        //헤더 있는페이지는 이거 필수
        List<MemberEntity> listteacher = memberRepository.findByTeacherCodeTeacher(member.getTeacherCode(),"teacher");
        MemberEntity teacher = listteacher.get(0);
        model.addAttribute("class-teacher", teacher);
        //여기 까지

        // 파일이 저장된 디렉토리
        String projectDir = Paths.get(System.getProperty("user.dir"), "file", "board", "free").toString();
        Path dirPath = Paths.get(projectDir);

        // 디렉토리가 존재하지 않으면 생성
        if (!Files.exists(dirPath)) {
            try {
                Files.createDirectories(dirPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String matchedFileName = null;
        boolean fileExists = false;

        // 디렉토리 내의 파일 목록을 검색
        try (Stream<Path> files = Files.list(dirPath)) {
            matchedFileName = files
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(name -> name.startsWith(uuid))
                    .findFirst()
                    .orElse(null);

            fileExists = (matchedFileName != null); // 파일 존재 여부 확인
        } catch (IOException e) {
            e.printStackTrace();
        }
        String filename = "";
        if(fileExists) {
            filename = matchedFileName.substring(36);
        }

        log.info("filename = {}", filename);
        model.addAttribute("fileExists", fileExists);
        model.addAttribute("filename", filename);
        model.addAttribute("originFilename", matchedFileName);
        model.addAttribute("free", free);
        model.addAttribute("subjectId", subjectId);
        model.addAttribute("today", LocalDate.now().toString());
        model.addAttribute("member", member);
        return "myClassUpdate";
    }

    @PostMapping("/update")
    public String freeUpdate(Model model,
                             @AuthenticationPrincipal UserDetails user,
                             @ModelAttribute BoardDTO.Update free,
                             @RequestParam("file") MultipartFile file) {

        MemberEntity member  = memberService.memberInfo(user.getUsername());

        boolean fileCheck = false;
        if (file != null && !file.isEmpty()) {
            fileCheck = true;
        }
        UUID uuid = UUID.randomUUID();
        FreeBoard save =null;
        if(fileCheck){
            save = freeBoardService.update(free.getTitle(), free.getContent(), uuid.toString(),free.getId());
            String projectDir = Paths.get(System.getProperty("user.dir"), "file", "board","free").toString();
            File directory = new File(projectDir);
            if (!directory.exists()) {
                boolean created = directory.mkdirs(); // 폴더 생성
                if (created) {
                    System.out.println("디렉토리 생성 성공: " + projectDir);
                } else {
                    System.err.println("디렉토리 생성 실패!");
                }
            } else {
                System.out.println("디렉토리가 이미 존재합니다: " + projectDir);
            }

            try {
                String originalFilename = file.getOriginalFilename();;
                String savePath = Paths.get(projectDir, uuid+originalFilename).toString();
                file.transferTo(new File(savePath));
                System.out.println("파일 저장 완료: " + savePath);
            } catch (IOException e) {
                e.printStackTrace();
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드 실패");
            }
        }else{
            //stay or delete
            save = freeBoardService.update(free.getTitle(), free.getContent(), free.getFileStatus(),free.getId());
        }



        return "redirect:/myClass?id=" + free.getClassId() + "&num=" + save.getId();
    }

    @GetMapping("/find")
    public ResponseEntity<?> getClassByCode(@RequestParam String code) {
        Subject subject = subjectService.findBySubjectId(code);
        log.info("ajax :"+code);
        if (subject!=null) {
            Optional<MemberEntity> teacher = memberService.findById(subject.getTeacherId());
            ClassDTO dto = new ClassDTO(subject);
            dto.setTeacherName(teacher.get().getName());
            return ResponseEntity.ok(dto);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @PostMapping("/classSave")
    public ResponseEntity<?> saveClasses(@RequestBody List<Map<String, Object>> cartList) {
        Map<String, String> response = new HashMap<>();
        try {
            for (Map<String, Object> item : cartList) {
                String classId = (String) item.get("classId");
                Integer memberId = (Integer) item.get("memberId");
                Long id=Long.valueOf(memberId);

                List<Attend> attends = attendService.findByMemberId(id);
                boolean already = false;
                for (Attend attend : attends) {
                    if(attend.getSubject().getId().equals(classId)){
                        already = true;
                        break;
                    }
                }

                if(!already){
                    attendService.save(memberService.findById(id).get(),subjectService.findBySubjectId(classId));
                    response.put("message", "장바구니 저장 완료");
                }else{
                    response.put("message", "이미 저장한 수업입니다!");
                }
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("저장 실패");
        }
    }

    @PostMapping("/delete")
    public ResponseEntity<Map<String, String>> deleteClass(@RequestBody Map<String, Object> requestData) {
        try {
            Long memberId = Long.valueOf(requestData.get("memberId").toString());
            String classId = requestData.get("classId").toString();

            attendService.memberSubjectDelete(memberId,classId);
            return ResponseEntity.ok(Collections.singletonMap("message", "수업이 삭제되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "삭제 중 오류가 발생했습니다."));
        }
    }
}
