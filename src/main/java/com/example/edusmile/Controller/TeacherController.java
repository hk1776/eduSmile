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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

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
            log.info("파일 이름 = {} 파일 정보 = {} 파일 사이즈 = {} 파일 타입 = {}",file.getOriginalFilename(), file.getContentType(), file.getSize(),file.getContentType());
            String projectDir = System.getProperty("user.dir");
            projectDir = Paths.get(projectDir, "file", "audio").toString();
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
//            log.info("request :"+stt.getText());
            String txt = "저는요 163번에요 163쪽 저기 1 1번에 밑에요 밑에 누구 아 밑에요 밑에 2번이요 2번 저건 뭐예요? 1 제곱 2제곱 3제곱 4제곱 이거는 등기 수역이라는 게 아니라 그냥 규칙성만 찾는 거야. 아니야 그건 이거는 그게 아니고 그냥 수열의 규칙성 찾는 거야. 그래서 n제곱이라고 써도 되잖아. 만약에 4제곱부터가 됐으면 어떻게 될까? 4제곱 9제곱 16제곱이었으면은 누구로 만들어야 돼? n 플러스 1의 제곱이라고 써야 되는 거야. 첫째로 넣을 때 4제곱이 2 제곱이 나와야 되니까 그럼 규칙성을 찾는 거야. 그리고 3번 해줬지 그거 10마이너스 1로 해가지고 10마이너스 2 아니야 3분의 1을 하면 아니 10마이너스 10의 n 제곱 마이너스 1에 9분의 1을 하면 11 11 111 이런 식으로 나오는 거야. 그래서 9분의 n 제곱 했고 n이 여기서 3이었던 거야. 9분의 n에 또 또요 그러면 이게 중차 수열이 a에는 a 플러스 n 마이너스 a 마이너스 a 정 10점 진짜 왜 어려워요? 어디 또 어디 나와 거친 소 171쪽이요 여기는 지금 되는 거지 해봤어 안 해봤어 어디까지 못 했니? 깎지 못했어. 근데 이건 알아 방금 봤는데 항의 개수하고 이거는 항수하고 똑같다고 그랬잖아. 169 해야 돼. 이제 172일이요. 너 다음 주 월요일날 오는 거야 이거 오늘 저기 하는 그러니까 해가지고 와 숙제 지금 내가 선생님이 이거를 지금 동차 세일 하루는 오늘로 끝냈대. 빨리빨리 아니 빨리 끝내는 게 아니라고 설명해. s 썸의 의미가 들어 있어 이게 썸이야 썸이 뭐지? 썸은 이게 무슨 소리 첫째 항부터 n1까지의 합을 이렇게 써. 그럼 등자세였을 때 뭐야 첫째 항을 a 두 번째 항은 뭐가 되겠어요? a 플러스 d가 되고 세 번째는 a 플러스 ED가 될 거고 아직 등비소리는 안 하는 거죠. 아직 안 했어 등비소리 떨렸나 보죠 해 봐. 이렇게 이렇게 들어가면 n 번째까지 가는 건 뭐야 a 플러스 n 마이너스 1 d 이렇게 쓰는 걸 SN이라고 하는 거야. 근데 얘가 마이너스 1이지 n번까지 가면 오케이 그러면 SN을 선생님이 이거 거꾸로 도할게. a 플러스 n 마이너스 1 d a 플러스 n 마이너스 ED a 이렇게 이렇게 하면 내가 지금 증명을 해주는 거야. 이게 뭐야 esn이지 이 하고 그러면 얘하고 더 하면 어 EA 플러스 n 마이너스 dg 이 하고 플러스 n 마이너스 d야 그다음에 이것도 a 만 시키고 그게 몇 개 있는 거야 MD 있는 거야. MD MD 그래서 SN은 2분의 상위 개수 e 플러스 n 마이너스 1 2 이렇게 SN이 이렇게 이루어지는 거예요. 그래서 이게 되는 거야. 그럼 이게 뭐냐 봐. 첫째 항이랑 마지막 항이 나와 있어. 그러면 2개 더 하면 다 똑같은 거지. 그래서 SN을 이 분해라고 쓰고 항의 개수 요게 욜로 오는 거야. 그리고 첫째 항 플러스 마지막 항이 나와 있으면 이렇게 해서 합을 계산을 하는 거야. 시험 방계가 너무 많은 24일 24일 날 보니 그 부 거의 다 볼 것 같은데 바로 해야지 그렇지 그렇지 완벽해 진짜 그러면 잘 봐. 내가 만약에 예를 들어서 이거 이해했지 등자지라은 어떻게 된다고 2분의 n EA 플러스 n 마이너스 d 아니면 첫째 형 플러스 마지막 항에 상위에서 n개를 곱한 거의 2분의 1로 나누는 거랑 똑같더라. 그러면 내가 SSN이라는 애가 a 플러스 a 2 a 3 an 이렇게 있어요. 이게 표현되는지 s n 마이너스 1이라는 게 뭐지? 첫째 형부터 n 마이너스 1 1까지의 합을 얘기하는 거야. 얘가 그럼 첫째 항 둘째 항 셋째 양 이렇게까지만 합한 거야. 여기서 여기를 빼잖아. 그러면 SN에서 SN 마이너스 1 빼면 다 사라지지 누구만 남아 일반 왕만 남는다는 얘기야. 이해했어 이게 무슨 소리냐고 첫째 항부터 n항까지야 이게 뭐야? 첫째 구하려면 이거 m트로스 mist 쓰면 되는 거 아니에요 아니지 그러니까 asn이 나와 있을 거야. 그럼 거기다가 n 마이너스 1을 대입한 거 함수라고 그랬잖아. 이거 n 마이너스 1을 대입한 걸 빼주면 al이 나오는 거야. 이해했지? 2항이 4 5항이 20 4 2분의 n 가호하고 EA n 마이너스 하면 되지 eaa가 첫 번째예요. 첫 번째 없잖아 초지면 a 그럼 저기서 공차를 보면 되겠구나. 2항부터 5항까지 몇 개의 공차가 있는 거지 1개 2개 3개 그렇죠 구하려고 그러는 걸 먼저 써놓는 거야. s h 303까지 합이니까 뭐야? s에 30이라고 하는 거는 첫 장부터 30번까지야 2분의 30 EA 플러스 29 d 이렇게 a랑 d를 계산하기 위해서 이게 나오는 거야. 그러니까 황이 2항까지가 4였으니까 s의 e는 2분의 2 EA 플러스 b 이게 뭐라고 4라고 그럼 EA 플러스 d가 얼마였다 4라는 얘기고 내가 구하려고 하는 걸 먼저 이렇게 구하려는 걸 써놓으면은 나온다고 이 지수가 그럼 이거랑 25항까지가 22였으니까 얘가 5가 될 거고 s의 5항은 이 분의 22 그렇게 하면 안 되지 않아요 뭐가 그렇게 하면 합계를 구하는 건데 저거는 5항이 25인 거지 합계가 안 그냥 전차해야지고 그럼 여긴 a하고 b를 갖다 집어넣으면 된다고 합의 보고 그대로 그려지 그다음에 2번 같은 경우는 두 수가 2하고 40 사이에 이건 저번에도 했었지 하고 44이에 항의 여기가 뭐야 n개가 있는 거야. 그럼 여기가 누구야 n 플러스 2번째 항이라고 그랬지 n 플러스 2 그다음에 이렇게 이루어졌을 때 421에 이거에 항의 합이 420이었다는 거야. 얘가 근데 첫째 항이랑 마지막 항이 나와 있으면 어떻게 된다고 그랬어 합을 그냥 계산이 된다고 그랬지. 그럼 이 꼴에 뭐야 항의 개수가 n 플러스 2번째 항까지 하지 그게 뭐야 이 더하기 40 이게 누구라고 420이 이렇게 하는 거야 여기는 42니까 10이 될 거고 n 플러스 2가 22 첫째 이랑 마지막 항이 있으면 2개 더 한 다음에 해야 되잖아. 2분의 2분의 저번에도 해줬잖아. 이거 그래서 어디를 하냐면 7시 1번 1번 2번 해봐. 귀찮아하지 말라고 1번부터 4가 아니 왜 작아지지 공채가 마이너스가 나네. 이거 난리가 나 첫 장부터 20분 첫째 한국적인 인식 평면 상행 수가 몇 개예요 철제 암부터 뭐라고 뭐 어디에 얘기하는데 n 플러스 2번 2번이죠. 이건 제 이건 말이 안 되는데 너무 어두운 거 몇 번 저 혼자 다 하고 있어요 그래 아니면 벌써 저 왜 이런거요 듣기로 했어요. 듣기로 요즘 BBC 같은 거 있잖아요. 통문장 쓰기에 봤거든요. 다 하니까 근데 우리 수학 수행평가 시험 끝나고 건데요. 이게 왜 이렇게 일관되게 보이면 좋지 않아 인생이 왜 이렇게 학교 민방 면 했단 말이에요. 라디오에서 하는 거 진짜 무슨 이 더운 날씨에 유동성에 모여가지고 진짜 아 애들 불만 많으면 많았겠는데 쉬는 시간에 했어요. 우리가 2시에 그게 딱 민방 요리 나왔거든요. 근데 2시 5분부터 쉬는 시간인데 오케이 첫 대형이 근데요 물어보는 게 아니고 확인하는 건데요. 72번에 첫째 형이랑 k형이 98인데 첫째 형이랑 저기 있으면요. k형이 98이니까 k를 벌 수 있지. 그러니까 그냥 ox 3 더하기 ox 한 다음에 x값 항의 개수 9평 보니까 ox가 95 그냥 o를 90으로 나누면 19 20평이겠네. 20개의 형이랑 첫 번째 형이랑 마지막 칸 더 번 재볼까 형이 이런 문제 못 해가지고 나이어 수영 되게 쉬워하는 애들도 있어. 그냥 눈으로 푸는 애들도 어려워지고 첫째 항부터 5항까지 합이 15고 구하려고 하는 게 뭐야? 20항까지 하면 2배 치죠. 2 배 치고 틀리면 아니야 아니 그런 소리 하지 마 뭐야 2분의 20에 EA 플러스 19d 이거 계산하는 거야. 그럼 a하고 b를 여기서 가지고 자고 구하려고 하는 걸 먼저 썰브라고 하는 이유가 그거야. 저 보통 사이 너무 어려운 것 같아요. 공통 수학 다시요 그럼 너 저기 뭐지 저기 누구지? 여기 시간이 남지 않아 오늘 목요일이지 목요일에 지역이 하나잖아요. 목요일날 뭐 있지? 영어 이 없어요 지연이 있어요. 오늘 지연이 와요. 제가 남는 게 시간이 잘 하는 사람이 9시에 못 볼 때 공통 수사로 갈 거야. 이 공통 사항이 이게 좀 어렵던데 3차 4차 야 너 그래서 세일 하라고 그랬잖아. 모르는 건 새한테 물어보라고 그랬지. 거의 안 하고 있어가지 거의 안 하고 있어가지고 왜 안 해? 이번 하지 말고 그냥 공동 수업할 여기서 쉬운데 뒤에 하기가 쉽지 않아. 친구로 오니 이거 얼마 안 남았어. 저기 얼마 안 남 여기 숙제 좀 하고 그냥 가면 알아. 그냥 가지 말고 혼자 가야 돼. 왜 지 멋대로 혀 너 집에 들어오면 몇 시야? 5시 5시 아니잖아 5시인데요. 4시 50분 정도에 들어오는 걸로 알고 있는데 5시에 들어와 딱 5시에 들어와. 그쯤에 들어가면 오늘 몇 시에 끝났는데 4시 반 훈련은 4시 한 35분에 끝나요. a하고 d만 잡아가지고 구하면 돼. 그다음에 잘 봐 최대로 된다라는 얘기는 너 100을 100이 있었어 거기에 이제 마이너스가 들어오기 시작을 하면 줄겠지. 숫자가 줄어서 항해 자꾸 더 한다고 다 계속 커지는 건 아니야. 그럼 뭐라고 하면 돼? 이게 공차가 마이너스 4였을 때 첫째 양이 14와 공짜와 마이너스 하이인 분자 수열이 nr까지 이건 그냥 눈으로 풀어도 되지. 첫째 양 14양 마이너스가 나오기 전까지만 계산을 하라는 얘기야. 그래야지 합이 최대가 돼. 뭐야 지 내서 지 뭐 그냥 직접 계산해서 풀겠는데 두 번째 양이 10이고 그렇게 저기하는 게 아니라 이제 이런 거만 나오는 건 아니잖아. 천 늦게 하면 안 돼요. 저 학교 가서 친하게 되는데 어떻게 친해요? 그럼 너 토요일 날 와. 아니 뭐 3일 아치는 토요일 날 한 번에 할 수 없는 노릇이니까. 그래서 어떻게 하자고 더 더 늦게 하면 안 되냐고 늦게 하려면 뭐 어떻게 할 건데 아니 우리 학교가 늦게 끝나니까 다들 일찍 끝나 학교 앞을 빼가지고 하면 안 돼요. 운동 중에도 올 수 있잖아. 얼룩 좀 하게. 근데 연락도 없네. 그냥 안 다니려나 봐. 걔 지금 조기 하는 걸로 알고 있는데 학원이라는 게 다니기도 하고 안 다니다 고 빨리 끝내야 이더라고요. 그런 소리 하지 말고 너를 조금 더 가서 쉬어. 그럼 6시 반에 애들 다 뒤로 미루면 되니까. 그럼 5시도 맨날 늦게 들어오잖아요. 10분씩. 아 그건 어쩔 수 없어요. 제가 원해가지고 학교를 1분 일찍 끝낼 수 없는.";
            Classification.STT stt = new Classification.STT();
            stt.setText(txt);
            String apiUrl = "http://127.0.0.1:8000/process_text/";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("text", stt.getText());

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);
            log.info("API Response Body: {}", response.getBody());

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("FastAPI 요청 실패: {}", response.getStatusCode());
                return "main";
            }
            Gson gson = new Gson();
            Classification.AnalyzeDTO send = gson.fromJson(response.getBody(), Classification.AnalyzeDTO.class);
            log.info("FastAPI 응답 객체: {}", send);

            log.info("받아온 값 :"+response.toString());
            List<Subject> subjects = subjectService.teacherSubject(member.getId());
            subjects.sort(Comparator
                    .comparing(Subject::getGrade) // 이름 기준 오름차순
                    .thenComparing(Subject::getDivClass));

            model.addAttribute("subject",subjects);
            model.addAttribute("stt",stt.getText());
            model.addAttribute("response", send);
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
