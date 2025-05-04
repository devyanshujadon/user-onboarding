package com.useronboarding.platform.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.useronboarding.platform.model.Comment;
import com.useronboarding.platform.model.Post;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostOrderByCreatedAtDesc(Post post);

    List<Comment> findByParentCommentIsNullAndPostOrderByCreatedAtDesc(Post post);

    List<Comment> findByParentCommentOrderByCreatedAtAsc(Comment parentComment);
}