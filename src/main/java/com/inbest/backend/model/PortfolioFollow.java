package com.inbest.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "portfoliofollow", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "portfolio_id"}))
public class PortfolioFollow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "portfolio_id", nullable = false)
    private Integer portfolioId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
} 