package com.inbest.backend.service;

import com.inbest.backend.model.Follow;
import com.inbest.backend.model.User;
import com.inbest.backend.repository.FollowRepository;
import com.inbest.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FollowService
{

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public void followUser(String followerName, String followingName)
    {
        if (followerName.equals(followingName))
        {
            throw new RuntimeException("You cannot follow yourself");
        }

        User follower = userRepository.findByUsername(followerName)
                .orElseThrow(() -> new RuntimeException("Follower not found"));

        User following = userRepository.findByUsername(followingName)
                .orElseThrow(() -> new RuntimeException("User to follow not found"));

        if (followRepository.existsByFollowerAndFollowing(follower, following))
        {
            throw new RuntimeException("Already following this user");
        }

        Follow follow = new Follow(follower, following);
        followRepository.save(follow);
    }

    @Transactional
    public void unfollowUser(String followerName, String followingName)
    {
        User follower = userRepository.findByUsername(followerName)
                .orElseThrow(() -> new RuntimeException("Follower not found"));

        User following = userRepository.findByUsername(followingName)
                .orElseThrow(() -> new RuntimeException("User to unfollow not found"));

        followRepository.deleteByFollowerAndFollowing(follower, following);
    }

    public List<User> getFollowing(String username)
    {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return followRepository.findByFollower(user)
                .stream()
                .map(Follow::getFollowing)
                .collect(Collectors.toList());
    }

    public List<User> getFollowers(String username)
    {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return followRepository.findByFollowing(user)
                .stream()
                .map(Follow::getFollower)
                .collect(Collectors.toList());
    }

    public Long getFollowerCount(String username)
    {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return followRepository.countByFollowing(user);
    }

    //maybe for future
    /*public Long getFollowingCount(Long userId)
    {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return followRepository.countByFollower(user);
    }*/

    public boolean isFollowing(String followerName, String followingName)
    {
        if (followerName.equals(followingName))
        {
            return false;
        }

        User follower = userRepository.findByUsername(followerName)
                .orElseThrow(() -> new RuntimeException("Follower not found"));

        User following = userRepository.findByUsername(followingName)
                .orElseThrow(() -> new RuntimeException("User to follow not found"));

        return followRepository.existsByFollowerAndFollowing(follower, following);
    }

}
