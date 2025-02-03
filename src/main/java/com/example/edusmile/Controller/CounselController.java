package com.example.edusmile.Controller;


import com.example.edusmile.Config.ClovaSpeechClient;
import com.example.edusmile.Dto.Classification;
import com.example.edusmile.Entity.CounselEntity;
import com.example.edusmile.Entity.MemberEntity;
import com.example.edusmile.Entity.Subject;
import com.example.edusmile.Entity.Summary;
import com.example.edusmile.Repository.CounselRepository;
import com.example.edusmile.Repository.MemberRepository;
import com.example.edusmile.Service.MemberService;
import com.example.edusmile.Service.SubjectService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.http.HttpSession;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Member;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
@Slf4j
@RequiredArgsConstructor
public class CounselController {

    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final CounselRepository counselRepository;
    private final SubjectService subjectService;

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

    @GetMapping("/counsel/Audio")
    public String record(@AuthenticationPrincipal UserDetails user, Model model) {
        MemberEntity member  = memberService.memberInfo(user.getUsername());
        model.addAttribute("member", member);
        model.addAttribute("teacher",member.getRole().equals("teacher"));

        //헤더 있는페이지는 이거 필수
        List<MemberEntity> listteacher = memberRepository.findByTeacherCodeTeacher(member.getTeacherCode(),"teacher");
        MemberEntity teacher = listteacher.get(0);
        model.addAttribute("class-teacher", teacher);
        //여기 까지

        if(!member.getRole().equals("teacher")) {
            return "main";
        }else{
            return "counselAudio";
        }
    }

    @PostMapping("/counsel/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   Model model,
                                   @AuthenticationPrincipal UserDetails user,
                                   RedirectAttributes redirectAttributes,
                                   HttpSession session) throws IOException {

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
//
//            final ClovaSpeechClient clovaSpeechClient = new ClovaSpeechClient();
//            ClovaSpeechClient.NestRequestEntity requestEntity = new ClovaSpeechClient.NestRequestEntity();
//            final String result =
//                    clovaSpeechClient.upload(convert, requestEntity);
//            JsonObject jsonObject = JsonParser.parseString(result).getAsJsonObject();
//            String textValue = jsonObject.get("text").getAsString();
//            Classification.STT stt = new Classification.STT();
//            stt.setText(textValue);
//            log.info("request :"+stt.getText());
            String txt = "안녕하세요. 그래 미나야 반갑다. 안녕하세요 그래 여기 상담실인데 미라가 지금 상담실에 왔잖아. 그래 왜 오게 된 건지 니가 생각하는 거 선생님이 좀 들을 수 있을까? 엄마가 가라고 그러는데 엄마가 뭐라고 하면서 가라고 하셨어? 학교 선생님이 가라고 했다고 그러는데 학교 선생님이 가라고 해서 그래 선생님이 두 가지 마음이 드네. 미라가 되게 싫었겠다 가라고 해서 그것도 엄마가 가라. 이것만 1단계도 싫은데 선생님이 가라 해서 엄마가 가라 해서 2단계를 거쳐서 왔잖아. 그게 좀 싫겠다 이 마음도 들고 또 한편 지금 미라랑 몇 마디 안 나누긴 했지만 야 너 6학년인데 솔직히 진짜 오기 싫으면 뭔 수를 써서라도 안 올 수도 있었잖아. 근데 니가 왔거든. 그래서 이건 그래도 미라 마음 안에 얼마큼의 조각일지 몰라도 그래도 상담실에 가볼까 이런 마음도 조금 있었을 것 같기도 하고 그런 마음들이 드네. 미라 마음은 어때요? 맞아 두 개 다 맞아 싫기도 하고 조금 가고 싶기도 하고 둘 다 맞아 아니면 보고 싶은 쪽이면서 두 번째 게 조금 더 맞아. 야 너 하이파이브 알지? 잠깐만 하자. 이건 좋은 거라서 일단 하고 설명해 줄게. 짠 좋아요. 상담실에 오고 싶어서 온 사람하고 코 깨가지고 진짜 아 내가 정말 죽지 못해 온다 이런 경우랑 되게 많이 다르거든. 그래서 니가 그래도 오고 싶은 마음이 있어서 왔다고 하면 그거는 앞으로 상담이 잘될 수 있는 좋은 징조 중에 하나야. 그래서 파이팅 의미로 하이파이브를 했고 그래 그러면 오고 싶은 마음이 있어서 왔다면 이제 선생님이 가라 했건 엄마가 뭐라 했건 간에 난 그건 모르겠다. 나는 미라의 이야기가 듣고 싶어 가 상담에 왜 오고 싶었고 상담에서 뭘 좀 하고 싶었을까 그걸 좀 듣고 싶네. 좀 듣고 싶네. 저요 제이 나무가 아니 떨어져요. 미라의 이야기를 사람들이 안 들어줘. 니 마음의 이야기를 그건 참 외롭고 쓸쓸한 건데 그래서 미라 표정이 이렇게 좀 쓸쓸하고 외로운 걸까? 그래 그러면 미라가 여기서 니 이야기를 좀 할 수 있고 선생님이 니 이야기를 그 네 이야기에 담긴 니 마음을 내가 좀 잘 들으면 좋겠구나 또 더 이야기해 볼까? 네 뭘 얘기해야 될지는 모르겠어요. 뭘 이야기해야 될지는 모르겠지만 우리가 이야기하는 것을 사람들이 잘 안 들어주니까 여기서는 좀 그냥 니 마음속에 있는 거 뭐든 이야기할 수 있었으면 좋겠구나. 그래 선생님이 마음으로 들으려고 노력할게요. 그러고 상담은 있잖아. 마음을 듣는 거거든. 그래서 아마 다른 데서보다는 니가 니 이야기를 좀 해도 될 것 같고 얼마큼 다를지 모르지만 좀 다를 거야. 그건 선생님이 약속할 수 있고 그리고 상담에서 제일 중요한 게 니가 하는 이야기 중에서 이건 해도 돼요라고 니가 이야기하거나 아니면 이거 엄마한테 이야기해도 되겠니? 이런 이런 이유에서 하고 싶은데 이걸 선생님한테 좀 전달해도 좋을까 이렇게 너한테 물어보고 니가 그렇게 하세요라고 할 때만 이야기하지. 여기서 너랑 나랑 나눈 이야기는 선생님이 무덤에 갈 때까지는 닫혀 있어. 그래서 그거를 선생님이 너한테 약속할 수 있어. 그래 무슨 이야기를 해야 될지 모르겠다고 하는데 그래도 요즘 니 마음이 어떤지 니가 어떻게 살고 있는지 니가 어떤 아이인지 선생님이 좀 알 수 있게 니가 해줄래 무슨 이야기일지 선생님도 모르겠고 모르겠지만 그냥 니 마음 가는 대로 아무 이야기나부터 시작한 다음에 그냥 막 아무 얘기나 해도 돼요. 심지어 BTS 이런 거 해도 된다. BTS 아세요? 니 나 조금 이래 보는 거 안다 내 왜 선생이 나이는 좀 있는데 그래도 국회에서는 알지 왜냐하면 진짜 우리 엄마는 그런 거 보면 막 혼내요. 방탄소년단 음악 그거를 듣는 걸 혼내신다고 듣는 거 보면 짜증 내고 맨날 컴퓨터하고 만 보고 있다고 제가 요즘 어떤 아이가 그러면 uts의 노래를 텔레비전을 하거나 유튜브로 봐야 되고 그렇지 엄마는 유튜브 보면 큰일 나는 줄 알았어. 유튜브 공포증이 있으신 모양이구나. 엄마 내가 뭐 하나 다 싫은가 봐요. 니가 뭐 하는 걸 엄마가 자꾸 못마땅하게 여기시고 뭐라고 하셔 그거 되게 짜증 나고 슬픈 일이다. 그지 니가 니가 좀 뭐랄까 니가 화를 그럴 때 화가 나는지 슬픈지 아마 두 개 다일 수 있는데 어떤지 모르겠다. 엄마가 니가 하는 뭐든지 못마땅히 하는 거 같고 못하게 하는 것 같다 싶으면 옛날에는 화도 내고 그랬는데요. 이제는 그냥 그러려니 해. 예전에는 화도 내보고 했는데 지금은 이제 그래봤자 소용도 없고 더 화나니까 화내니까 니가 화를 내면 엄마가 또 더 또 한술 더 뜨시니까 그냥 그러려니 하고 그러려니 해요라고 니가 말을 하네. 포기했어요가 아니라 그러려니 해요 하니 우리 6학년이니까 포기했어요 하고 그러려니 해요 하고 좀 다른 건 알지? 포기한 건 그래 엄마는 안 된다 이렇게 이제 진짜 싫지만 그냥 두는 거고 그러려니는 그래 우리 엄마는 저러는구나 이렇게 하고서 이제 니 마음에서 이렇게 흘려보내는 거라서 탁 닫아버리는 거랑 흘려보내는 게 좀 다른 것 같아 손이 닫았어 달았어 해봤는데 어떻게 포기가 아니라 그러려니 한다 이런 마음이 들까 저한테 그게 그거예요. 너한텐 그게 그거야 그러면 이제 포기하고서 그래도 할 건 하니 아니면은 못하게 하는 건 냅두고 다른 거를 하려고 애써보고 별로 할 게 없니 저는 아무것도 안 하고 싶어요. 뭐 니가 하려고 하는 거마다 하지 마 하지 마 하지 마 하지 마 이걸 계속하면 아무것도 안 하고 싶어지는 게 보통 사람 마음이지 일하는 게 아니라 근데 엄마는 그건 뭔지 알고 이거 하고 있노 이러시겠네. 근데 제가 좀 잘못하긴 해요. 맨날 맨날 뭐 일만 하니까 BTS 보는 것도 엄마가 싫어하시고 게임도 싫어하실 건데 게임은 하고 BTS는 보지 말라면 안 보나 어쩌면 나는 니 집에서 뭐 하는지 궁금하다. 그냥 아무 생각이 안 나고 그런 거 안 하면 안 싶고 할 게 없어요. 그래도 있으니까 제가 공부를 잘하는 것도 아니고 말썽만 부리고 선생님이 전화하고 이러니까 제가 좀 잘못한 것도 니가 하려고 하는 거를 그렇게 하지 말라는 소리를 많이 들었지만 그래도 게임만 많이 하고 또 공부 별로 열심히 하지 않고 학교에서 선생님한테 한 소리 듣는 그런 행동들을 주로 하고 이거는 잘못됐다고 니가 생각하는구나. 친구들이 다 너를 싫어해. 저는 친구가 없어요. 학교에서 보내는 시간이 긴데 친구들이 다 너를 싫어한다. 나는 친구가 없다. 그렇게 생각되면 학교에서도 니가 즐거움이 없겠다. 야 학교도 너무 가기가 싫어요. 학교에도 가기 싫고 그럴 만할 것 같다. 학교에 가기 싫을 것 같다. 근데 니 결석 많다는 말은 내가 못 들었는데 그래 니 많이 누워 있다 소리를 선생님한테 내가 전해 들었다. 그래 누워 있으면 선생님이 또 뭐라 뭐라 하시나 학교 취향이 좀 그래서 그냥 주는 그래서 니 이모 한번 보자. 레이머 콤플렉스입니다. 아니 너무 누워 있어서 혹시 내가 잘못된 건가 싶어가지고 그런 건 없네. 그 정도는 아니 피부 탄력이 좋구먼. 피부는 너무 안 좋아서 고르는데 피부가 안 좋아서 고민이냐 그래 자연스러운 거다. 피부 고민하는 건 니가 청소년이 되어간다는 이야기네. 다 커버할 수 있다. 이게 중요하다. 눈 코 입 생김새 이거 이게 중요하다. 이게 원판이 안 좋으면 수술해도 잘 안 되거든. 원판이 괜찮네. 엄마 닮았나 아빠 닮았나 눈 골은 그 이야기하니까 또 표정이 자꾸 웃네. 엄마 아빠 생각하기 싫다. 아빠 닮았다. 엄마 아빠랑 사이 안 좋으신 걸 내가 알겠구먼 이혼. 그럼 니가 아빠 닮은 거가 어떤 때는 엄마를 더 화나게 하는 거 같다 싶을 때가 있겠네. 아이 엄마가 너를 미워하는데 살기는 니가 지금 엄마하고 살고 있네. 그럼 왜 아빠한테 확 보내버리고 이렇게는 안 하시구나 갔다가 다시 아빠한테 갔다가 왔어. 니가 왔어 어떻게 된 거야? 아빠가 제 여자친구 생겨서 키우고 있다고 해서 다시 엄마한테 온 거예요. 엄마는 다시 받아주셨고 그래 그 사이에서 니가 또 좀 마음의 상처가 많았겠다. 그리고 그런 상황 속에서 니가 게임 말고 뭘 할 수 있었겠나 그거 말고는 참 집중할 게 없었을 것 같아. 그때 마음이 많이 상했구나. 누가 안 그렇겠노? 누구라도 그런 상황에서는 마음이 많이 안 좋지. 근데 미라야 그런 상황에서 니가 게임이 그래도 너한테는 낙이 돼 줬잖아. 그래서 게임을 하는 거를 너무 잘못이라고 생각하진 않았으면 좋겠다. 엄마가 싫어하면 자동으로 잘못된 행동이고 아까 그 판단하는 것 같은데 BTS 싫어하는 거 그거는 엄마가 잘못된 거라고 BTS 좋아하는 거는 잘못된 게 아니라고 네가 판단하는 것 같던데. 근데 게임은 이제 엄마가 싫어하기 때문에 잘못된 게 아니라 엄마도 싫어하지만 내가 생각해도 너무 많이 하는 건 좀 x다 이렇게 생각하는 거 판단하는 거 아니다. 제가 공부도 좀 더 잘하고 반장도 하고 친구들하고도 잘 지내고 그런 괜찮았어요. 혹은 누구 이야기예요? 네 이야기예요. 엄마 생각이에요. 둘 다예요. 그렇게 한번 생각해 봅시다. 앞으로는 왜냐하면 니가 판단할 수 없는 사람이 아니라 보니까 니가 판단이 있네 생각이 있고. 근데 엄마 거랑 네 거가 좀 섞여 있는 게 있는 것 같다. 더 하면 수가 많아지잖아. 그러면은 더 하면 무거워지지 그럼 니가 감당하기 어려워지잖아. 그러니까 더 하지 말고 뺄 건 빼고 곱하기 전 하지 마요. 엄마 거 했다는 거 이거 봐라. 지금도 니 지금 뭐 생각하고 있지 하면서 니 판단력이 되게 좋다니까 솔직히 있잖아. 니 많이 엎드려 있다 했잖아. 게임보다 재미있기는 되게 어려운데 학교 수업이 게임에 버금갈 정도로 좀 재미있고 유익하다고 생각되면 우리 계속 엎드려 있겠나 해봐라. 나는 니가 판단력이 있다고 생각한다. 그러고 지금 현재 니가 게임 많이 해요. 공부 별로 안 해요. 학교에서 주로 많이 엎드려 있어요. 수업 별로 열심히 안 해요. 그리고 친구들이 나를 싫어하고 친구가 없어요. 이런 것들이 내가 많이 많이 마음이 힘들어. 나는 내 마음에 상처를 많이 받아서 마음의 기운이 없어요. 그것들에 내가 기운을 마음의 기운을 거의 다 썼어요. 그런 이야기 같아. 괜히 너무 힘들었다. 그지 엄마 아빠가 이혼하신 거는 너 몇 살 때야? 초등학교 2학년 그래 초등학교 2학년이 겪기에 그게 그래 만만한 일인가 얼마나 힘들었겠노. 그다음에 엄마한테 있다가 아빠한테 갔다가 또 엄마한테 다시 오고 이게 또 보통 일이가 니가 힘들었지 아까 이제는 힘이 없어요. 그래 그러니까 힘이 없을 만한 그래도 크긴 잘 컸다 보자. 마음의 기운은 없어도 키도 아빠 닮았나 아빠 이야기 안 하고 싶어. 아빠 닮은 것도 싫고 아빠 이야기가 싫으면 되는 아이고 아빠가 미라한테 상처를 많이 주셨구먼 여기까지 하고 그만할게. 그래 웃기도 하네. 웃으니까 좀 예쁘네. 미라야 그래 우리 만나서 니가 어디서도 해서 별로 이렇게 사람들이 안 들어주던 그래서 받았던 그 이야기를 편하게 이것저것 하자. 그리고 하면서 니가 나한테 마음껏 이야기하고 혹시 내가 마음껏 잘 못 듣거나 아니면은 선생님이 나이 많다 보니까 그랬잖아. 나이 많아서 엄마 집 비슷한 거 하거든 싫타이리 엄마처럼 하실 거예요. 아니 나도 모르게 하다가 보면 소발에 쥐잡기로 너그 엄마가 하는 거랑 비슷한 거 할 수도 있잖아. 혹시나 해서 말해줘야 되고 그래서 니가 좀 마음이 좀 편해지고 가벼워지고 좀 그랬으면 좋겠어. 그리고 니가 아무도 내 이야기를 안 들어줘요 하고 여기 온 건 우리가 마음의 기운이 많이 지금 없기는 하지만 니가 그냥 지금처럼 이렇게 살고 싶지 않은 마음이 있는 것 같아. 어떤 노 니 원하는 대로 다 된다면 어떻게 살고 싶어 친구랑 잘 지내고 친구랑 잘 지내고 또 공부도 지금보다 잘하고 싶고 성격도 지금보다 더 마음에 들게 좀 바꿨으면 좋겠고 또그래서 엄마가 좋아하면 좋겠어요. 그래서 엄마가 나를 좋아해 줬으면 좋겠구나. 그러면 이제 니가 순서대로 만나 봐 봐. 친구들하고 잘 지냈으면 좋겠고, 공부도 지금보다 더 잘했으면 좋겠고 이 까먹었다. 세 번째 뭐였노? 친구랑 잘 지내고 공부 잘하고 성격 그냥 게임 게임도 좀 줄였으면 좋겠고 만약에 만약에요. 그렇게 해도 엄마가 별로 좋아하지 않는다면 이 앞에 게 다 쓸모없어. 그래도 이거는 나한테 중요해요야. 그건 너무 슬퍼. 그래 너무 슬프지. 난 지금 더하기 빼기 이야기를 너한테 다시 지금 하고 있는 건데 보해줬잖아. 엄마 부분을 빼도 네 부분 숫자가 있어 아니면은 니 건 0이고 무조건 엄마가 좋아하기 위해서 이거를 하는 거야. 근데 엄마가 좋아하면 좋겠어요. 좋아하면 더 좋겠어요. 엄마가 좋아하면 좋겠어요. 엄마가 좋아하면 좋겠어요. 엄마가 너한테 되게 중요하구나. 엄마가 너한테 참 중요해. 니 마음이 참 따뜻하구나. 엄마가 너한테 신경질 많이 냈을 텐데 말이야. 그래도 여전히 엄마가 너한테 소중하다는 걸 니가 알고 있고 엄마가 나한테 소중하기 때문에 엄마가 나를 좋아해 줬으면 좋겠다. 엄마가 이 미라가 이렇게 착하고 엄마를 소중히 여기는 딸이라는 거를 꼭 아셔야 될 낀데 말이야. 같이 한번 해볼까요? 좋아요. 할 만한 할 가치가 있는 프로젝트인 것 같아요. 프로젝트란 말 알지? 야를 무슨 프로젝트라고 프로젝트 이름을 하나 더 짓고 그런 건 잘 몰라 그래 선생님이 그럼 한번 생각해 볼게 뭐 좀 있어 보이잖아. 프로젝트 이름도 막 있고 이러면 지금은 그냥 그냥 임시로 붙인 이름으로 니가 일하니까 프로젝트 m 어떻니? 나 오케이 프로젝트 하이파이브 다시 한번 그래 이 시간 괜찮아 미라야 좋아요그래 그러면 이 시간에 다시 만나기로 하고 별일이 없으면 같은 요일 같은 시간이 우리가 제일 편하니까 한 요일에 한 번 그러니까 일주일에 한 번 같은 시간에 만나는 걸로 하고 만나면 한 50분 조금 더 짧을 수도 있고 조금 더 길 수도 있고 돈은 엄마가 돈은 너 그것도 신경 쓰는구나. 그래 그건 어쨌든 보호자랑 이야기해야 되는데 선생님이 의뢰했기 때문에 아마 어쩌면 학교에 어떤 좀 그런 예산이 있을지도 몰라. 그래서 그거는 일단 지금 니가 걱정할 필요는 없을 것 같아. 그건 어른들이 알아서 할게. 그건 보통 돈은 어른들이 알아서 하는 게 맞아. 너는 잘 돼서 나중에 다른 방식으로 갚으면 돼. 그래 오늘 이제 우리가 프로젝트를 어떤 내용을 노력해야 될지를 한번 생각해 봤고 그리고 대충 이제 임시로 이름도 하나 붙였다. 그지 혹시 이 프로젝트에 니가 덧붙여야 될 거 추가해야 될 거 있어아. 목소리도 커졌으면 좋겠어. 항상 크면 안 되는데 언제 좀 컸으면 좋겠어 말할 때 목소리가 작아지고 학교에서 집에서 엄마한테는 집에서는 괜찮고 학교에서 좀 컸으면 좋겠다. 친구들이랑 이야기할 때 혹은 발표할 때 언제가 더 필요해 둘 다 이 밀라요요 앙큼한 거 졸업 수업 시간에 엎드려 있으면서 발표도 크게 하고 싶구나. 이 내가 이거 하니까 니가 웃었는데 너 봉마 이거 하나 아이고 아이고 이거 이런 말 놀리는 거 같아. 놀릴 마음 일 있다. 일도 없다가 아니라 일이 있다. 그냥 니가 좀 웃는 걸 보고 싶었어. 니가 웃으면 되게 다른 거 알아. 거울 봐 언제 봐 혼자 있을 때도 학교가 우리한테 거울 볼 때 한 번씩 두 번씩 웃으려고 노력해 보고 와라라고 선생님이 숙제를 내주면 할 거야. 안 할 거야 그럼 학교 공부는 열심히 안 해도 인생 공부는 되게 열심히 하는 모범생인데 인생 공부가 더 중요하고 이 세상이 더 큰 학교거든. 같이 한번 학교 수업 시간에는 배울 수 없는 더 중요한 공부를 같이 한번 해봅시다. 그래 오늘 이제 첫 상담을 했는데 미라가 기분이 좀 어떤지 어떤 마음이 드는지 이 상담에 대해서 미라가 한번 상해 볼까요? 마무리를 한번 해볼까요? 얘기할 때가 있어서 좋아. 그렇구나. 참 그게 니가 고팠다. 그지 오늘 첫날이라서 니한테 뭐 많이 물었는데 그래도 이야기할 수 있어서 좋았어. 다음 시간에 니가 더 많이 이야기해라. 알았지? 그래 나는 미라를 만나서 진짜로 반가웠고 미라가 원하는 거를 이루도록 선생님이 최선을 다해서 너하고 옆에 같이 있을게. 예절 바르기도 하셔라. 그러고 일단 학교에서 있잖아 학교에서도 거울 볼 수 있나? 그래 그러면 집에서 많이 웃어보고 오도록 하고 그러고 다른 거는 니가 하던 대로 해. 뭘 너무 성급하게 바꾸려고 하면 또 부모라는 사람들은 또 막 기대하고 막 앞서간다. 그럼 니가 힘들 수도 있거든. 우리 좀 천천히 가자. 처음에는 그래 선생님은 그래 어떤 거 같아 이야기 좀 할 수 있을 거 같나? 그래 그러면 우리 다음 같은 시간에 두자.";
            Classification.STT stt = new Classification.STT();
            stt.setText(txt);
            String apiUrl = "https://edusmile-fastapi-bbd6che3gpg6fuhx.koreacentral-01.azurewebsites.net/process_counsel_text/";
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
            Classification.Counsel send = gson.fromJson(response.getBody(), Classification.Counsel.class);
            log.info("FastAPI 응답 객체: {}", send);

            log.info("받아온 값 :"+send.getSummary().getText());
            List<Subject> subjects = subjectService.getMemberSubject(member.getId());
            subjects.sort(Comparator
                    .comparing(Subject::getGrade) // 이름 기준 오름차순
                    .thenComparing(Subject::getDivClass));

            redirectAttributes.addFlashAttribute("subject", subjects);
            redirectAttributes.addFlashAttribute("stt", stt.getText());
            redirectAttributes.addFlashAttribute("response", send.getSummary().getText());
            redirectAttributes.addFlashAttribute("member", member);
            redirectAttributes.addFlashAttribute("teacher", member.getRole().equals("teacher"));
            session.setAttribute("lastUploadedResponse", send.getSummary().getText().get(0));
            session.setAttribute("lastUploadedSTT", stt.getText());
            session.setAttribute("lastSubjects", subjects);
            session.setAttribute("lastMember", member);
            return "redirect:/counsel/upload";
        }
    }

    @GetMapping("/counsel/upload")
    public String redirectUpload(Model model,@AuthenticationPrincipal UserDetails user,HttpSession session) {

        // Flash Attribute 데이터 가져오기
        if (model.containsAttribute("response")) {
            return "counselResult";
        }
        // Flash Attribute가 없을 때 세션 데이터 활용 (새로고침 대비)
        String lastResponse = (String) session.getAttribute("lastUploadedResponse");
        List<Subject> lastSubjects = (List<Subject>) session.getAttribute("lastSubjects");
        String lastSTT = (String) session.getAttribute("lastUploadedSTT");
        MemberEntity lastMember = (MemberEntity) session.getAttribute("lastMember");
        List<String> lastExplains = (List<String>) session.getAttribute("lastExplains");

        log.info("lastResponse: "+lastResponse);
        if(lastExplains != null){
            model.addAttribute("explains", lastExplains);
        }
        if (lastResponse != null) {
            model.addAttribute("response", lastResponse);
            model.addAttribute("stt", lastSTT);
        }
        if (lastSubjects != null) {
            model.addAttribute("subject", lastSubjects);
        }
        if (lastMember != null) {
            model.addAttribute("member", lastMember);
        }

        return "classResult";
    }

    @PostMapping("/counsel/summary")
    public ResponseEntity<?> submitSummary( @AuthenticationPrincipal UserDetails user,
                                            @RequestPart(value = "files", required = false) List<MultipartFile> files,
                                            @RequestParam("content") String content,
                                            @RequestParam("subjectId") String subjectId) {
        log.info(content);
        log.info(subjectId);

        return ResponseEntity.ok("ok");
    }

}
