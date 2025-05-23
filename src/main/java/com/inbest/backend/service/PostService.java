package com.inbest.backend.service;

import com.inbest.backend.dto.*;
import com.inbest.backend.exception.UserNotFoundException;
import com.inbest.backend.model.InvestmentActivity;
import com.inbest.backend.model.Post;
import com.inbest.backend.model.User;
import com.inbest.backend.repository.InvestmentActivityRepository;
import com.inbest.backend.repository.LikeRepository;
import com.inbest.backend.repository.PostRepository;
import com.inbest.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService
{
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final InvestmentActivityRepository investmentActivityRepository;
    private final FollowService followService;
    private final LikeRepository likeRepository;
    private final UserService userService;

    @Transactional
    public PostResponseDTO createPost(PostCreateDTO postDTO)
    {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        InvestmentActivity investmentActivity = investmentActivityRepository.findById(postDTO.getInvestmentActivityId())
                .orElseThrow(() -> new IllegalStateException("Investment activity not found"));

        Post post = new Post();
        post.setContent(postDTO.getContent());
        post.setUser(user);
        post.setInvestmentActivity(investmentActivity);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setIsTrending(false);


        Post savedPost = postRepository.save(post);
        return convertToDTO(savedPost);
    }

    public List<PostResponseDTO> getAllPosts()
    {
        return postRepository.findAllPublicPosts().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<PostResponseDTO> getPostsFromFollowedUsers(int page, int size)
    {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        List<FollowDTO> followedUsers = followService.getFollowing(username);
        List<User> users = followedUsers.stream()
                .map(followDTO -> userRepository.findByUsername(followDTO.getUsername())
                        .orElseThrow(() -> new UserNotFoundException("User not found")))
                .collect(Collectors.toList());

        List<Post> posts = postRepository.findPublicPostsByUsersOrderByCreatedAtDesc(users);
        if (posts.isEmpty()) {
            return new ArrayList<>();
        }

        int startIndex = (page - 1) * size;
        if (startIndex >= posts.size()) {
            return new ArrayList<>();
        }

        return posts.stream()
                .skip(startIndex)
                .limit(size)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<PostResponseDTO> getPostById(Long id)
    {
        return postRepository.findById(id)
                .map(this::convertToDTO);
    }

    public List<PostResponseDTO> getPostsByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUser = auth != null ? auth.getName() : null;
        List<Post> posts;

        if (currentUser.equals(username)) {
            posts = postRepository.findByUser(user);
        } else {
            posts = postRepository.findAllPublicPostsByUsername(username);
        }

        return posts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deletePost(Long id)
    {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Post not found"));
        postRepository.delete(post);
    }

    @Transactional
    public void updateAllPostScores()
    {
        List<Post> posts = postRepository.findAll();
        for (Post post : posts)
        {
            double score = calculatePostScore(post);
            post.setTrendScore(score);
        }
        postRepository.saveAll(posts);
    }

    private double calculatePostScore(Post post)
    {
        // Get post age in hours
        long minutesSinceCreation = ChronoUnit.MINUTES.between(post.getCreatedAt(), LocalDateTime.now());
        int hoursSinceCreation = (int) Math.ceil(minutesSinceCreation / 60.0);

        // Weight factors
        double likeWeight = 1.0;
        double commentWeight = 2.0; // Comments are weighted more than likes
        double timeDecayFactor = 0.95; // Score decays over time

        // Calculate base score
        double baseScore = (post.getLikeCount() * likeWeight) +
                (post.getCommentCount() * commentWeight);

        // Apply time decay
        double timeDecay = Math.pow(timeDecayFactor, hoursSinceCreation);

        return baseScore * timeDecay;
    }

    public List<PostResponseDTO> getTrendingPosts(int page, int size)
    {
        List<Post> posts = postRepository.findAllPublicOrderByScoreDesc();
        if (posts.isEmpty()) {
            return new ArrayList<>();
        }

        int startIndex = (page - 1) * size;
        if (startIndex >= posts.size()) {
            return new ArrayList<>();
        }

        return posts.stream()
                .skip(startIndex)
                .limit(size)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<PostResponseDTO> getCurrentUserPosts()
    {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return postRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


    private PostResponseDTO convertToDTO(Post post)
    {
        User user = post.getUser();
        InvestmentActivity activity = post.getInvestmentActivity();
        boolean isLiked = false;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String username = auth.getName();
            User currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            isLiked = likeRepository.existsByUserIdAndPostId(currentUser.getId(), post.getId());
        }

        UserDTO userDTO = UserDTO.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .name(user.getName())
                .surname(user.getSurname())
                .image_url(user.getImageUrl())
                .build();


        InvestmentActivityResponseDTO activityDTO = InvestmentActivityResponseDTO.builder()
                .activityId(activity.getActivityId())
                .portfolioId(activity.getPortfolio().getPortfolioId())
                .stockId(activity.getStock().getStockId())
                .stockSymbol(activity.getStock().getTickerSymbol())
                .stockName(activity.getStock().getStockName())
                .actionType(activity.getActionType().name())
                .stockQuantity(activity.getStockQuantity())
                .date(activity.getDate())
                .old_position_weight(activity.getOldPositionWeight())
                .new_position_weight(activity.getNewPositionWeight())
                .build();


        PostResponseDTO dto = new PostResponseDTO();
        dto.setId(post.getId());
        dto.setContent(post.getContent());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setStockSymbol(activity.getStock().getTickerSymbol());
        dto.setLikeCount(post.getLikeCount());
        dto.setCommentCount(post.getCommentCount());
        dto.setTrending(post.getIsTrending());
        dto.setUserDTO(userDTO);
        dto.setInvestmentActivityResponseDTO(activityDTO);
        dto.setLiked(isLiked);
        return dto;
    }

    public List<PostResponseDTO> getPostsByPortfolio(Long portfolioId)
    {
        List<Post> posts = postRepository.findPostsByPortfolioId(portfolioId);

        return posts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public long getTotalTrendingPostsCount() {
        return postRepository.count();
    }

    public long getTotalFollowedPostsCount() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        List<FollowDTO> followedUsers = followService.getFollowing(username);
        List<User> users = followedUsers.stream()
                .map(followDTO -> userRepository.findByUsername(followDTO.getUsername())
                        .orElseThrow(() -> new UserNotFoundException("User not found")))
                .collect(Collectors.toList());

        return postRepository.findPublicPostsByUsersOrderByCreatedAtDesc(users).size();
    }
}
