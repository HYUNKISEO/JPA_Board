package com.lec.spring.domain;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.Type;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "t7_authority")
public class Authority {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // pk
    private String name; // 권한명  ex)"ROLE_MEMBER", "ROLE_ADMIN"

    @ManyToMany(fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<User> userList = new ArrayList<>();

}

