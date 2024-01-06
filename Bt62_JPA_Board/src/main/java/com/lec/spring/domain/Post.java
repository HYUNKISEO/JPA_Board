package com.lec.spring.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lec.spring.domain.Entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Model 객체
/**
 * DTO 객체
 *  : Data Transfer Object 라고도 함.
 *
 *  객체 -> DB
 *  DB -> 객체
 *  reg param -> 객체
 *  ..
 *
 */

// 웹개발시...
// 가능한, 다음 3가지는 이름을 일치시켜주는게 좋습니다.
// 클래스 필드명 = DB 필드명 = form의 name명

@Data   // 게터, 세터
@NoArgsConstructor   // 기본생성자
@AllArgsConstructor  // 전부담은 생성자
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Builder
@Entity(name = "t7_post")
public class Post extends BaseEntity {   // 대문역할 데이터를 주고 받는 중심지 ..?
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // 글 id (pk)
    private String subject;
    private String content;

    private Long viewCnt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;  // 글 작성자 (FK)

    // 첨부파일
    @OneToMany(mappedBy = "post", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    private List<Attachment> fileList = new ArrayList<>();

    @OneToMany(mappedBy = "post", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    @JsonIgnore
    private List<Comment> comments = new ArrayList<>();


}
