package com.inbest.backend.repository;

import com.inbest.backend.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Set;

public interface CommentRepository extends JpaRepository<Comment, Integer>
{

    @Query("select c from Comment c" +
            " where c.post.id = :postId")
    Set<Comment> findByPostId(Integer postId);
}
