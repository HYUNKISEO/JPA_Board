package com.lec.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing // 스프링에서 제공하는 기분 Listener 사용하기 위해 필요
public class Bt62_JPA_Board {

    public static void main(String[] args) {
        SpringApplication.run(Bt62_JPA_Board.class, args);
    }

}
