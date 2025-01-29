package com.example.edusmile.Controller;


import com.example.edusmile.Entity.MemberEntity;
import com.example.edusmile.Repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequiredArgsConstructor
public class MemberRestController {

    private final MemberRepository memberRepository;

    private static final String UPLOAD_DIR = "C:/uploads/"; // 외부 폴더

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
}
