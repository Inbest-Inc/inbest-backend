package com.inbest.backend.repository;

import com.inbest.backend.model.Follow;
import com.inbest.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FollowRepository extends JpaRepository<Follow, Long>
{
    boolean existsByFollowerAndFollowing(User follower, User following);

    void deleteByFollowerAndFollowing(User follower, User following);

    List<Follow> findByFollower(User follower);

    List<Follow> findByFollowing(User following);

    Long countByFollowing(User following);

    Long countByFollower(User follower);
}
