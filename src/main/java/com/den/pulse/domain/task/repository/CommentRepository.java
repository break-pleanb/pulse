package com.den.pulse.domain.task.repository;

import com.den.pulse.domain.task.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

    List<Comment> findByTask_IdOrderByCreatedAtAsc(UUID taskId);
}
