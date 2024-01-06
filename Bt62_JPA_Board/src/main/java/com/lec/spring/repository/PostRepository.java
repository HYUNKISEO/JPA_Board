package com.lec.spring.repository;

// repository layer (aka. Data layer)
// Datasource 에 직접 접근

import com.lec.spring.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.awt.print.Pageable;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

}
