package com.inbest.backend.repository;

import com.inbest.backend.model.Post;
import com.inbest.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface PostRepository extends JpaRepository<Post, Long>
{
    List<Post> findByUser(User user);
}