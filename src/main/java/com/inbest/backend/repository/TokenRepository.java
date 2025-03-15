package com.inbest.backend.repository;

import com.inbest.backend.model.Token;
import com.inbest.backend.model.User;
import com.inbest.backend.model.TokenType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long>
{
    Optional<Token> findByToken(String token);

    Optional<Token> findByUserAndTokenType(User user, TokenType tokenType);
}
