package com.example.edusmile.Controller;


import com.example.edusmile.Entity.Attend;
import com.example.edusmile.Entity.MemberEntity;
import com.example.edusmile.Entity.Subject;
import com.example.edusmile.Repository.AttendRepository;
import com.example.edusmile.Repository.MemberRepository;
import com.example.edusmile.Repository.SubjectRepository;
import com.example.edusmile.Service.AttendService;
import com.example.edusmile.Service.BlobService;
import com.example.edusmile.Service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
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


    private static final String UPLOAD_DIR = "C:/uploads/"; // 외부 폴더

    private final BlobService blobService;
    private final MemberService memberService;


    @PostMapping("/uploadsprofile")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file,
                                        @RequestParam("id") Long id) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("파일이 비어 있습니다.");
        }


        try {
            String fileUrl = blobService.uploadProfile(file,id);

            return ResponseEntity.ok("File uploaded successfully: " + fileUrl);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File upload failed.");
        }

    }

    @GetMapping("/signed-url")
    public String getSignedUrl(@RequestParam String fileName) {
        return blobService.generateSignedUrl(fileName);
    }

    @DeleteMapping("/trueDeleteAccount")
    public ResponseEntity<String> deleteAccount(@AuthenticationPrincipal User user) {

        MemberEntity member = memberService.memberInfo(user.getUsername());

        memberService.deleteAccount(member);



        return ResponseEntity.ok("ok");
    }
}
