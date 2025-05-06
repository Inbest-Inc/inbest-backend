package com.inbest.backend.repository;

import com.inbest.backend.model.Post;
import com.inbest.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long>
{
    List<Post> findByUser(User user);

    @Query("""
                 SELECT p FROM Post p
                 WHERE p.investmentActivity.portfolio.visibility = 'public'
                 ORDER BY p.trendScore DESC
            """)
    List<Post> findAllPublicOrderByScoreDesc();


    @Query("""
    SELECT p FROM Post p
    WHERE p.user IN :users
      AND p.investmentActivity.portfolio.visibility = 'public'
    ORDER BY p.createdAt DESC
    """)
    List<Post> findPublicPostsByUsersOrderByCreatedAtDesc(@Param("users") List<User> users);

    List<Post> findByUserOrderByCreatedAtDesc(User user);

    @Query("""
            SELECT p
            FROM Post p
            JOIN FETCH p.investmentActivity ia
            JOIN FETCH ia.stock s
            JOIN FETCH p.user u
            WHERE ia.portfolio.portfolioId = :portfolioId
            ORDER BY p.createdAt DESC
            """)
    List<Post> findPostsByPortfolioId(@Param("portfolioId") Long portfolioId);
    @Query("""
    SELECT p FROM Post p
    WHERE p.investmentActivity.portfolio.visibility = 'public'
    ORDER BY p.createdAt DESC
    """)
    List<Post> findAllPublicPosts();
    @Query("""
    SELECT p FROM Post p
    WHERE p.investmentActivity.portfolio.visibility = 'public'
      AND p.user.username = :username
    ORDER BY p.createdAt DESC
    """)
    List<Post> findAllPublicPostsByUsername(@Param("username") String username);
}