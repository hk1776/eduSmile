package com.example.edusmile.Controller;
import com.example.edusmile.Config.ClovaSpeechClient;
import com.example.edusmile.Dto.Classification;
import com.example.edusmile.Entity.MemberEntity;
import com.example.edusmile.Entity.Subject;
import com.example.edusmile.Service.MemberService;
import com.example.edusmile.Service.PostService;
import com.example.edusmile.Service.SubjectService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/teacher")
public class TeacherController {
    private final MemberService memberService;
    private final PostService postService;
    private final SubjectService subjectService;

    @GetMapping()
    public String home(@AuthenticationPrincipal UserDetails user, Model model) {
        MemberEntity member  = memberService.memberInfo(user.getUsername());
        model.addAttribute("member", member);
        model.addAttribute("teacher",member.getRole().equals("teacher"));
        if(!member.getRole().equals("teacher")) {
            return "main";
        }else{
            return "teacher";
        }
    }

    @GetMapping("/class")
    public String record(@AuthenticationPrincipal UserDetails user, Model model) {
        MemberEntity member  = memberService.memberInfo(user.getUsername());
        model.addAttribute("member", member);
        model.addAttribute("teacher",member.getRole().equals("teacher"));
        if(!member.getRole().equals("teacher")) {
            return "main";
        }else{
            return "class";
        }
    }

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   Model model,
                                   @AuthenticationPrincipal UserDetails user) throws IOException {

        MemberEntity member  = memberService.memberInfo(user.getUsername());
        if(!member.getRole().equals("teacher")) {
            return "main";
        }else {
            if (!Objects.equals(file.getContentType(), "audio/mp3")) {
                log.info("파일 형식 에러" + file.getContentType());
            }
//            log.info("파일 이름 = {} 파일 정보 = {} 파일 사이즈 = {} 파일 타입 = {}",file.getOriginalFilename(), file.getContentType(), file.getSize(),file.getContentType());
//            String projectDir = System.getProperty("user.dir");
//            File convert = new File(projectDir, Objects.requireNonNull(file.getOriginalFilename()));
//            log.info(convert.getPath());
//            file.transferTo(convert);
//            log.info("valid ={}",isValidMp3(convert));
//            final ClovaSpeechClient clovaSpeechClient = new ClovaSpeechClient();
//            ClovaSpeechClient.NestRequestEntity requestEntity = new ClovaSpeechClient.NestRequestEntity();
//            final String result =
//                    clovaSpeechClient.upload(convert, requestEntity);
//            JsonObject jsonObject = JsonParser.parseString(result).getAsJsonObject();
//            String textValue = jsonObject.get("text").getAsString();
//            Classification.STT stt = new Classification.STT();
//            stt.setText(textValue);
//
//            String url = "Azure AI model Server URL";
//            String send = postService.sendPostRequest(url, stt);
            Gson gson = new Gson();
            String send = "{\n" +
                    "  \"notice\": \"이 강의 내용에는 공지사항이 없습니다.\",\n" +
                    "  \"class_summary\": \"이 수업에서는 인공지능, 딥러닝, 머신러닝의 개념과 차이점을 설명하고 있습니다. 핵심 내용을 요약하면 다음과 같습니다:\\n\\n1. 인공지능(Artificial Intelligence, AI):\\n   - 가장 상위 개념으로, 기계가 인간의 지능적 행동을 모방하여 문제를 해결하는 것을 의미합니다.\\n   - 자율주행, 알파고, 이미지 생성, 언어 모델 등이 인공지능의 대표적인 예시입니다.\\n\\n2. 딥러닝(Deep Learning):\\n   - 심층 신경망 구조를 가지고 있어 깊이가 깊다는 것이 특징입니다.\\n   - 복잡한 문제를 해결할 수 있도록 설계된 신경망 구조를 활용합니다.\\n\\n3. 머신러닝(Machine Learning):\\n   - 딥러닝을 포함하는 개념으로, 데이터로부터 특징을 추출하고 알고리즘을 통해 문제를 해결합니다.\\n   - 지도학습, 비지도학습, 강화학습 등 다양한 방식의 학습 알고리즘을 사용합니다.\\n\\n4. 학습 방식의 종류:\\n   - 지도학습(Supervised Learning): 문제와 정답이 모두 주어지는 학습 방식\\n   - 비지도학습(Unsupervised Learning): 문제만 주어지고 정답을 스스로 찾아내는 학습 방식\\n   - 준지도학습(Semi-supervised Learning): 일부 문제-정답 쌍이 주어지는 학습 방식\\n   - 강화학습(Reinforcement Learning): 문제만 주어지고 피드백을 통해 최적의 해답을 찾아가는 학습 방식\\n\\n이와 같이 인공지능, 딥러닝, 머신러닝은 서로 연관된 개념이지만 세부적인 차이가 있음을 알 수 있습니다. 각각의 개념에 대한 구체적인 예시를 통해 이해를 돕고 있습니다.\",\n" +
                    "  \"quiz\": {\n" +
                    "    \"questions\": [\n" +
                    "      {\n" +
                    "        \"question\": \"인공지능, 딥러닝, 머신러닝 중 가장 상위 개념은?\",\n" +
                    "        \"choices\": [\n" +
                    "          \"인공지능\",\n" +
                    "          \"딥러닝\",\n" +
                    "          \"머신러닝\",\n" +
                    "          \"이들은 모두 동등한 개념\"\n" +
                    "        ],\n" +
                    "        \"correct_answer\": 0,\n" +
                    "        \"explanation\": \"인공지능이 가장 상위 개념이며, 딥러닝과 머신러닝은 인공지능을 구현하는 기술들이다.\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"question\": \"딥러닝의 특징은?\",\n" +
                    "        \"choices\": [\n" +
                    "          \"매우 간단한 신경망 구조\",\n" +
                    "          \"얕은 신경망 구조\",\n" +
                    "          \"깊은 신경망 구조\",\n" +
                    "          \"하나의 레이어로 구성\"\n" +
                    "        ],\n" +
                    "        \"correct_answer\": 2,\n" +
                    "        \"explanation\": \"딥러닝은 심층신경망을 사용하여 깊은 구조의 신경망을 가지고 있다.\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"question\": \"머신러닝에 포함되는 것은?\",\n" +
                    "        \"choices\": [\n" +
                    "          \"딥뉴럴 네트워크 활용\",\n" +
                    "          \"알고리즘 추가\",\n" +
                    "          \"유의미한 구조 파악\",\n" +
                    "          \"이 모두가 포함된다\"\n" +
                    "        ],\n" +
                    "        \"correct_answer\": 3,\n" +
                    "        \"explanation\": \"딥뉴럴 네트워크 활용, 알고리즘 추가, 유의미한 구조 파악 등이 모두 머신러닝에 포함된다.\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"question\": \"지도학습의 특징은?\",\n" +
                    "        \"choices\": [\n" +
                    "          \"문제와 답이 모두 주어진다\",\n" +
                    "          \"문제만 주어지고 답은 없다\",\n" +
                    "          \"문제와 답이 일부만 주어진다\",\n" +
                    "          \"문제와 답이 모두 없다\"\n" +
                    "        ],\n" +
                    "        \"correct_answer\": 0,\n" +
                    "        \"explanation\": \"지도학습에서는 문제와 답이 모두 주어진다.\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"question\": \"강화학습의 특징은?\",\n" +
                    "        \"choices\": [\n" +
                    "          \"문제와 답이 모두 주어진다\",\n" +
                    "          \"문제만 주어지고 답은 없다\",\n" +
                    "          \"문제와 답이 일부만 주어진다\",\n" +
                    "          \"문제를 풀면 점수를 받는다\"\n" +
                    "        ],\n" +
                    "        \"correct_answer\": 3,\n" +
                    "        \"explanation\": \"강화학습에서는 문제를 풀면 점수를 받아 학습하는 방식이다.\"\n" +
                    "      }\n" +
                    "    ]\n" +
                    "  }\n" +
                    "}";
            Classification.AnalyzeDTO response = gson.fromJson(send, Classification.AnalyzeDTO.class);
            List<Subject> subjects = subjectService.teacherSubject(member.getId());
            subjects.sort(Comparator
                    .comparing(Subject::getGrade) // 이름 기준 오름차순
                    .thenComparing(Subject::getDivClass));

            model.addAttribute("subject",subjects);
            model.addAttribute("stt",send);
            model.addAttribute("response", response);
            model.addAttribute("member", member);
            model.addAttribute("teacher",member.getRole().equals("teacher"));

            return "classResult";
        }
    }
    public boolean isValidMp3(File file) {
        try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file)) {
            AudioFormat format = audioInputStream.getFormat();
            return format != null && format.getEncoding() == AudioFormat.Encoding.PCM_SIGNED;
        } catch (Exception e) {
            return false;
        }
    }
}
