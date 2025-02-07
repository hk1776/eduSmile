package com.example.edusmile.Service;

import com.azure.storage.blob.*;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.example.edusmile.Entity.MemberEntity;
import com.example.edusmile.Repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import static java.lang.System.getenv;

@Service
public class BlobService {

    private final BlobServiceClient blobServiceClient;
    private final MemberRepository  memberRepository;
    private final String containerName;
    Map<String,String> env = getenv();

    @Autowired
    public BlobService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
        this.blobServiceClient = new BlobServiceClientBuilder().connectionString(env.get("connectionString")).buildClient();
        this.containerName = env.get("containerName");
    }


    public String uploadProfile(MultipartFile file,Long id) throws IOException {
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String filePath = id+extension;
        BlobClient blobClient = containerClient.getBlobClient(filePath);

        // 파일 업로드
        blobClient.upload(file.getInputStream(), file.getSize(), true);

        MemberEntity member = memberRepository.findById(id).orElse(null);

        member.setImg_path(id+extension);

        memberRepository.save(member);

        return blobClient.getBlobUrl(); // 업로드된 파일 URL 반환
    }


    public byte[] downloadFile(String fileName) throws IOException {
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
        BlobClient blobClient = containerClient.getBlobClient(fileName);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        blobClient.downloadStream(outputStream);
        return outputStream.toByteArray();
    }


    public void deleteFile(String fileName) {
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
        BlobClient blobClient = containerClient.getBlobClient(fileName);

        blobClient.delete();
    }



    //이거 컨테이너에 엑세스 가능하게(사진)
    public String generateSignedUrl(String blobName) {

        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);


        BlobClient blobClient = containerClient.getBlobClient(blobName);


        BlobSasPermission permission = new BlobSasPermission().setReadPermission(true); // 읽기 권한만 부여
        OffsetDateTime expiryTime = OffsetDateTime.now().plus(1, ChronoUnit.HOURS); // 1시간 후 만료
        BlobServiceSasSignatureValues values = new BlobServiceSasSignatureValues(expiryTime, permission);

        // Signed URL 반환
        String sasToken = blobClient.generateSas(values);
        return blobClient.getBlobUrl() + "?" + sasToken;
    }

}