package com.inbest.backend.scheduler;

import com.inbest.backend.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScoreScheduler {

    private final PostService postService;

    // For every hours use 3600000
    @Scheduled(fixedRate = 3600000)
    public void updatePostScores() {
        postService.updateAllPostScores();
    }
}