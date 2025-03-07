package com.inbest.backend.repository;

import com.inbest.backend.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Integer>
{
}
