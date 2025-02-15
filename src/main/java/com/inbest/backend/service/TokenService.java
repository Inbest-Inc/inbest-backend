package com.inbest.backend.service;

import com.inbest.backend.model.Token;
import com.inbest.backend.model.TokenType;
import com.inbest.backend.model.User;
import com.inbest.backend.repository.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class TokenService
{
    @Autowired
    private TokenRepository tokenRepository;

    public String createToken(User user, TokenType tokenType, int expiresIn)
    {
        tokenRepository.findByUserAndTokenType(user, tokenType).ifPresent(token -> {
            if (!token.isExpired())
            {
                throw new IllegalStateException("Token already exists");
            }
            tokenRepository.delete(token);
        });

        String newtoken = UUID.randomUUID().toString(); //bu yeni token generate ediyor
        Token token = new Token();
        token.setToken(newtoken);
        token.setUser(user);
        token.setTokenType(tokenType);
        token.setExpiryDate(LocalDateTime.now().plusMinutes(expiresIn));
        tokenRepository.save(token);
        return newtoken;
    }

    public Optional<Token> validateToken(String token, TokenType tokenType)
    {
        return tokenRepository.findByToken(token)
                .filter(t -> !t.isExpired() && t.getTokenType().equals(tokenType));
    }

    public void invalidateToken(String token)
    {
        tokenRepository.findByToken(token).ifPresent(tokenRepository::delete);
    }
}
