package com.lec.spring.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lec.spring.domain.Entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Builder
@Entity(name = "t7_user")
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    @JsonIgnore
    private String password;
    @ToString.Exclude  // toString() 결과에서 뺌. (제외?)
    @JsonIgnore
    private String re_password;  // 비밀번호 확인 입력 받아오기용
    private String name;
    private String email;

    @ManyToMany(fetch = FetchType.EAGER)
    @ToString.Exclude
    private final List<Authority> authority = new ArrayList<>();

    // OAuth2
    private String provider;    // 어떤 OAuth2 제공자?  kakao, naver, google ...
    private String providerId;  // provider 내에서의 고유 id 값

}
