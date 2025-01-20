package com.example.edusmile.Controller;


import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.SecureRandom;
import java.util.Map;

import static java.lang.System.getenv;

@RestController
public class SMSController {

    final DefaultMessageService messageService;



    Map<String,String> env = getenv();

    public SMSController() {
        // 반드시 계정 내 등록된 유효한 API 키, API Secret Key를 입력해주셔야 합니다!
        this.messageService = NurigoApp.INSTANCE.initialize( env.get("SMS_API_KEY"),  env.get("SMS_SECRET_KEY"), "https://api.coolsms.co.kr");
    }


    /**
     * 단일 메시지 발송 예제
     */
    @PostMapping("/user/send-one")
    public int sendOne(@RequestBody String phoneNumber) {
        System.out.println("Sending message");

        Message message = new Message();
        SecureRandom secureRandom = new SecureRandom();
        int code = 100000 + secureRandom.nextInt(900000);
        // 발신번호 및 수신번호는 반드시 01012345678 형태로 입력되어야 합니다.
        message.setFrom("01072715939");
        message.setTo(phoneNumber);
        message.setText("["+code + "][EduSmile] 인증번호를 입력해 주세요.");




        SingleMessageSentResponse response = this.messageService.sendOne(new SingleMessageSendingRequest(message));
        System.out.println(response);

        return code;
    }


}

