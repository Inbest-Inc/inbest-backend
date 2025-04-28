package com.inbest.backend.repository;

import com.inbest.backend.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Set;

public interface CommentRepository extends JpaRepository<Comment, Integer>
{

    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId ORDER BY c.createdDate DESC")
    Set<Comment> findByPostId(Integer postId);

}
