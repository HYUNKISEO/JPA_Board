package com.lec.spring.service;

import com.lec.spring.domain.Authority;
import com.lec.spring.domain.User;
import com.lec.spring.repository.AuthorityRepository;
import com.lec.spring.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuthorityRepository authorityRepository;

    // username(회원 아이디) 로 user 정보 읽어오기
    @Override
    public User findByUsername(String username) {
        User user = userRepository.findByUsername(username);
        return user;
    }

    // 특정 username(회원 아이디) 의 회원이 존재하는지 확인
    @Override
    public boolean isExist(String username) {
        User find = userRepository.findByUsername(username);

        boolean check = find != null ? true : false;

        return check;
    }

    // 신규회원 등록
    @Override
    public int register(User user) {
        // DB 에는 회원 username 을 대문자로 저장
        user.setUsername(user.getUsername().toUpperCase());

        // password 는 암호화 해서 저장.  PasswordEncoder 객체 사용
        user.setPassword(passwordEncoder.encode(user.getPassword()));
         userRepository.save(user);  // 새로운 회원 (User) 저장. id값 받아옴.

        // 미리 생성된 권한 가져오기
        Authority member = authorityRepository.findByName("ROLE_MEMBER");
        Authority admin = authorityRepository.findByName("ROLE_ADMIN");

        // 권한이 없으면 생성
        if (member == null) {
            member = new Authority();
            member.setName("ROLE_MEMBER");
            authorityRepository.save(member);
        }

        if (admin == null) {
            admin = new Authority();
            admin.setName("ROLE_ADMIN");
            authorityRepository.save(admin);
        }

        // 신규회원은 ROLE_MEMBER 권한을 부여하기
        user.getAuthority().add(member);
        userRepository.save(user);

//        // 신규회원은 ROLE_MEMBER 권한을 부여하기
//        Authority auth = new Authority();
//        auth.setName("ROLE_MEMBER");
//        authorityRepository.save(auth);
//
//        user.getAuthority().add(auth);
//        userRepository.save(user);

        return 1;
    }

    //특정 사용자(id) 의 authority(들)
    @Override
    public List<Authority> selectAuthoritiesById(Long id) {
        User user = userRepository.findById(id).orElse(null);
        return user != null ? user.getAuthority() : Collections.EMPTY_LIST;
    }

}
