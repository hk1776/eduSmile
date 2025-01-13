package com.example.edusmile.Service;

import com.example.edusmile.Entity.MemberEntity;
import com.example.edusmile.Repository.MemberRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserSecurityService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Getter
    public enum UserRole {
        ADMIN("ROLE_ADMIN"),
        USER("ROLE_USER");

        UserRole(String value) {
            this.value = value;
        }

        private String value;
    }

    @Override          //로그인 인증 기능
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        Optional<MemberEntity> _siteUser = this.memberRepository.findByloginId(loginId);
        if (_siteUser.isEmpty()) {
            throw new UsernameNotFoundException("사용자를 찾을수 없습니다.");
        }

        MemberEntity siteUser = _siteUser.get();
        List<GrantedAuthority> authorities = new ArrayList<>();
        if ("admin".equals(loginId)) {
            authorities.add(new SimpleGrantedAuthority(UserRole.ADMIN.getValue()));
        } else {
            authorities.add(new SimpleGrantedAuthority(UserRole.USER.getValue()));
        }
        return new User(siteUser.getLoginId(), siteUser.getPassword(), authorities);
    }
}
