package com.lec.spring.repository;

import com.lec.spring.domain.Attachment;
import com.lec.spring.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Map;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
}
