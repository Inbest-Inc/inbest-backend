package com.inbest.backend.repository;

import com.inbest.backend.model.Portfolio;
import com.inbest.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

<<<<<<< Updated upstream
import java.util.List;
=======
import java.util.Optional;
>>>>>>> Stashed changes

public interface PortfolioRepository extends JpaRepository<Portfolio, Long>
{
    Optional<Portfolio> findByPortfolioId(int portfolioId);
    boolean existsByPortfolioName(String portfolioName);

    List<Portfolio> findByUser(User user);
}
