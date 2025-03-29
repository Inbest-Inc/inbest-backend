package com.inbest.backend.controller;

import com.inbest.backend.dto.InvestmentActivityCreateDTO;
import com.inbest.backend.dto.InvestmentActivityResponseDTO;
import com.inbest.backend.service.InvestmentActivityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.EntityNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/investment-activities")
@RequiredArgsConstructor
public class InvestmentActivityController {
    private final InvestmentActivityService investmentActivityService;

    @PostMapping
    public ResponseEntity<?> createActivity(@Valid @RequestBody InvestmentActivityCreateDTO dto) {
        try {
            InvestmentActivityResponseDTO activity = investmentActivityService.createActivity(dto);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Investment activity created successfully");
            response.put("data", activity);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(404).body(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

//    @GetMapping   Do not need to get all investment activites
//    public ResponseEntity<?> getAllActivities() {
//        try {
//            List<InvestmentActivityResponseDTO> activities = investmentActivityService.getAllActivities();
//            Map<String, Object> response = new HashMap<>();
//            response.put("status", "success");
//            response.put("message", "Investment activities retrieved successfully");
//            response.put("data", activities);
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {
//            Map<String, String> response = new HashMap<>();
//            response.put("status", "error");
//            response.put("message", e.getMessage());
//            return ResponseEntity.badRequest().body(response);
//        }
//    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getActivityById(@PathVariable Long id) {
        try {
            InvestmentActivityResponseDTO activity = investmentActivityService.getActivityById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Investment activity not found"));
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Investment activity retrieved successfully");
            response.put("data", activity);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(404).body(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/portfolio/{portfolioId}")
    public ResponseEntity<?> getActivitiesByPortfolioId(@PathVariable Integer portfolioId) {
        try {
            List<InvestmentActivityResponseDTO> activities = investmentActivityService.getActivitiesByPortfolioId(portfolioId);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Portfolio activities retrieved successfully");
            response.put("data", activities);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(404).body(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteActivity(@PathVariable Long id) {
        try {
            investmentActivityService.deleteActivity(id);
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Investment activity deleted successfully");
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(404).body(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
} 