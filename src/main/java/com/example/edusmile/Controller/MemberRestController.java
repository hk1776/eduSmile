package com.example.edusmile.Controller;


import com.example.edusmile.Entity.Attend;
import com.example.edusmile.Entity.MemberEntity;
import com.example.edusmile.Entity.Subject;
import com.example.edusmile.Repository.AttendRepository;
import com.example.edusmile.Repository.MemberRepository;
import com.example.edusmile.Repository.SubjectRepository;
import com.example.edusmile.Service.AttendService;
import com.example.edusmile.Service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Member;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class MemberRestController {

    private final MemberRepository memberRepository;
    private final AttendService attendService;
    private static final String UPLOAD_DIR = "C:/uploads/"; // 외부 폴더
    private final AttendRepository attendRepository;
    private final SubjectRepository subjectRepository;

    @PostMapping("/uploadsprofile")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file,
                                        @RequestParam("id") Long id) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("파일이 비어 있습니다.");
        }

        try {

            // 실제 저장 경로
            String basePath = UPLOAD_DIR+"profile_img/";
            File directory = new File(basePath);

            // 디렉토리 존재 여부 확인 및 생성
            if (!directory.exists()) {
                directory.mkdirs();
            }



            // 파일 저장 경로
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filePath = basePath + id+extension;
            System.out.println(filePath);

            File existFile = new File(filePath);
            if (existFile.exists()) {
                System.out.println("!");
                if (existFile.delete()) {
                    System.out.println("기존 파일 삭제 성공");
                } else {
                    System.out.println("기존 파일 삭제 실패");
                }
            }


            file.transferTo(new File(filePath));


            MemberEntity member = memberRepository.findById(id).orElse(null);

            member.setImg_path("/profile_img/"+id+extension);

            memberRepository.save(member);

            return ResponseEntity.ok("파일 업로드 성공: " + filePath);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("파일 업로드 중 오류 발생: " + e.getMessage());
        }
    }

    @GetMapping("/trueDeleteAccount")
    public ResponseEntity<String> deleteAccount(@AuthenticationPrincipal User user) {

        Optional<MemberEntity> mem = memberRepository.findByloginId(user.getUsername());
        MemberEntity member = mem.get();

        if(member.getRole().equals("teacher"))
        {
            List<MemberEntity> members = memberRepository.findByRoleAndTeacherCode("student",member.getTeacherCode());

            for(MemberEntity memberEntity : members)
            {
                memberEntity.setTeacherCode("&");
                memberRepository.save(memberEntity);
            }

            List<Subject> subjects = subjectRepository.findSubjectByTeacherId(member.getId());

            System.out.println(subjects.size());

            for(Subject subject : subjects)
            {
                String subjectcode = subject.getId();

                attendRepository.deleteAttendBySubjectId(subjectcode);

            }
            for(Subject subject : subjects)
            {
                String subjectcode = subject.getId();

                subjectRepository.deleteSubjectById(subjectcode);


            }

            memberRepository.deleteById(member.getId());

        }
        else if(member.getRole().equals("student"))
        {
            memberRepository.delete(member);
        }



        return ResponseEntity.ok("ok");
    }
}
