package com.inbest.backend.controller;

import com.inbest.backend.model.PortfolioFollow;
import com.inbest.backend.model.response.GenericResponse;
import com.inbest.backend.service.PortfolioFollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/portfolio/follow")
@RequiredArgsConstructor
public class PortfolioFollowController {

    private final PortfolioFollowService portfolioFollowService;

    @PostMapping
    public ResponseEntity<?> followPortfolio(@RequestParam Integer portfolioId) {
        try {
            portfolioFollowService.followPortfolio(portfolioId);
            return new ResponseEntity<>(new GenericResponse("success", "Portfolio followed successfully", portfolioId), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()));
        }
    }

    @DeleteMapping
    public ResponseEntity<?> unfollowPortfolio(@RequestParam Integer portfolioId) {
        try {
            portfolioFollowService.unfollowPortfolio(portfolioId);
            return new ResponseEntity<>(new GenericResponse("success", "Portfolio unfollowed successfully", portfolioId), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()));
        }

        }

    @GetMapping("/check")
    public ResponseEntity<?> isFollowing(@RequestParam Integer portfolioId) {
        try {
            boolean isFollowing = portfolioFollowService.isFollowing(portfolioId);
            return new ResponseEntity<>(new GenericResponse("success", isFollowing ? "You are following this portfolio" : "You are not following this portfolio", isFollowing), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()));
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> getFollowedPortfolios() {
        try {
            List<PortfolioFollow> followedPortfolios = portfolioFollowService.getFollowedPortfolios();
            return new ResponseEntity<>(new GenericResponse("success", "Followed portfolios retrieved successfully", followedPortfolios), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()));
        }
    }

    @GetMapping("/followers")
    public ResponseEntity<?> getPortfolioFollowers(@RequestParam Integer portfolioId) {
        try {
            List<PortfolioFollow> followers = portfolioFollowService.getPortfolioFollowers(portfolioId);
            return new ResponseEntity<>(new GenericResponse("success", "Portfolio followers retrieved successfully", followers), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()));
        }
    }
} 