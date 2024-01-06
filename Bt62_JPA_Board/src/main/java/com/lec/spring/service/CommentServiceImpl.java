package com.lec.spring.service;

import com.lec.spring.domain.*;
import com.lec.spring.repository.CommentRepository;
import com.lec.spring.repository.PostRepository;
import com.lec.spring.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostRepository postRepository;


    // 특정 글(id) 의 댓글 목록
    @Override
    public QryCommentList list(Long id) {
        QryCommentList list = new QryCommentList();

        List<Comment> comments = commentRepository.findByPostId(id);

        list.setCount(comments.size());  // 댓글의 갯수
        list.setList(comments);
        list.setStatus("OK");

        return list;
    }

    // 특정 글 (postId) 에 특정 사용자(userId) 가 댓글 작성
    @Transactional
    @Override
    public QryResult write(Long postId, Long userId, String content) {

        User user = userRepository.findById(userId).orElse(null);

        Post post = postRepository.findById(postId).orElse(null);

        Comment comment = Comment.builder()
                .user(user)
                .content(content)
                .post(post)
                .build();

        commentRepository.save(comment);

        QryResult result = QryResult.builder()
                .count(1)
                .status("OK")
                .build();

        return result;
    }

    // 특정 댓글(id) 삭제
    @Override
    public QryResult delete(Long id) {
        int count = 0;
        Comment comment = commentRepository.findById(id).orElse(null);
        if(comment != null){
            count++;
            commentRepository.deleteById(id);
        }

        String status = "FAIL";

        if(count > 0) status = "OK";

        QryResult result = QryResult.builder()
                .count(count)
                .status(status)
                .build();

        return result;
    }
}
