package com.example.edusmile.Service;

import com.example.edusmile.Entity.MemberEntity;
import com.example.edusmile.Repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {
    private final MemberRepository memberRepository;

    public MemberEntity memberInfo(String loginId) {
        Optional<MemberEntity> member = memberRepository.findByloginId(loginId);
        return member.orElse(null);
    }
    public Optional<MemberEntity> findById(Long id) {
        return memberRepository.findById(id);
    }
    public void saveSubject(Long id, String subject) {
        Optional<MemberEntity> member = memberRepository.findById(id);
        String memberSubject = member.get().getSubject();
        if(memberSubject==null || memberSubject.isEmpty()) {
            List<String>list = new ArrayList<String>();
            list.add(subject);
            member.get().setSubject(list.toString());
        }else {
            String trimmed = memberSubject.substring(1, memberSubject.length() - 1);
            List<String> list = new ArrayList<> (Arrays.asList(trimmed.split(", ")));
            list.add(subject);
            member.get().setSubject(list.toString());
        }
        memberRepository.save(member.get());
    }
    public MemberEntity deleteSubject(Long id, String subject) {
        Optional<MemberEntity> member = memberRepository.findById(id);
        String memberSubject = member.get().getSubject();

        String trimmed = memberSubject.substring(1, memberSubject.length() - 1);
        List<String> list = new ArrayList<> (Arrays.asList(trimmed.split(", ")));
        list.remove(subject);
        member.get().setSubject(list.toString());

        return memberRepository.save(member.get());
    }
}
