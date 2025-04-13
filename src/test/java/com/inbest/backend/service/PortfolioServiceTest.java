package com.inbest.backend.service;

import com.inbest.backend.dto.PortfolioDTO;
import com.inbest.backend.model.Portfolio;
import com.inbest.backend.model.User;
import com.inbest.backend.model.response.PortfolioGetResponse;
import com.inbest.backend.repository.PortfolioRepository;
import com.inbest.backend.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PortfolioServiceTest {

    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private PortfolioService portfolioService;

    private User testUser;
    private Portfolio testPortfolio;
    private PortfolioDTO testPortfolioDTO;
    private static final String TEST_USERNAME = "testuser";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // SecurityContext setup
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn(TEST_USERNAME);

        // Create test user
        testUser = new User();
        testUser.setId(1);
        testUser.setUsername(TEST_USERNAME);
        testUser.setName("Test");
        testUser.setSurname("User");

        // Create test portfolio
        testPortfolio = new Portfolio();
        testPortfolio.setPortfolioId(1);
        testPortfolio.setPortfolioName("Test Portfolio");
        testPortfolio.setVisibility("public");
        testPortfolio.setCreatedDate(LocalDateTime.now(ZoneId.of("UTC")));
        testPortfolio.setLastUpdatedDate(LocalDateTime.now(ZoneId.of("UTC")));
        testPortfolio.setUser(testUser);

        // Create test portfolio DTO
        testPortfolioDTO = new PortfolioDTO();
        testPortfolioDTO.setPortfolioName("Test Portfolio");
        testPortfolioDTO.setVisibility("public");

        // Setup common mocks
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(testUser));
    }

    @AfterEach
    void tearDown() {
        reset(portfolioRepository, userRepository, securityContext, authentication);
        SecurityContextHolder.clearContext();
    }

    @Test
    void doesPortfolioNameExist_shouldReturnTrue_whenPortfolioNameExists() {
        // given
        String portfolioName = "Existing Portfolio";
        when(portfolioRepository.existsByPortfolioName(portfolioName)).thenReturn(true);

        // when
        boolean result = portfolioService.doesPortfolioNameExist(portfolioName);

        // then
        assertTrue(result);
        verify(portfolioRepository).existsByPortfolioName(portfolioName);
    }

    @Test
    void doesPortfolioNameExist_shouldReturnFalse_whenPortfolioNameDoesNotExist() {
        // given
        String portfolioName = "Non-existing Portfolio";
        when(portfolioRepository.existsByPortfolioName(portfolioName)).thenReturn(false);

        // when
        boolean result = portfolioService.doesPortfolioNameExist(portfolioName);

        // then
        assertFalse(result);
        verify(portfolioRepository).existsByPortfolioName(portfolioName);
    }

    @Test
    void createPortfolio_shouldReturnPortfolioId_whenSuccessful() {
        // given
        when(portfolioRepository.existsByPortfolioName(testPortfolioDTO.getPortfolioName())).thenReturn(false);
        when(portfolioRepository.save(any(Portfolio.class))).thenReturn(testPortfolio);

        // when
        int result = portfolioService.createPortfolio(testPortfolioDTO);

        // then
        assertEquals(testPortfolio.getPortfolioId(), result);
        verify(portfolioRepository).save(any(Portfolio.class));
    }

    @Test
    void createPortfolio_shouldThrowException_whenPortfolioNameExists() {
        // given
        when(portfolioRepository.existsByPortfolioName(testPortfolioDTO.getPortfolioName())).thenReturn(true);

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> portfolioService.createPortfolio(testPortfolioDTO));

        assertEquals("Portfolio name already exists!", exception.getMessage());
        verify(portfolioRepository, never()).save(any(Portfolio.class));
    }

    @Test
    void updatePortfolio_shouldUpdatePortfolio_whenSuccessful() {
        // given
        int portfolioId = 1;
        PortfolioDTO updatedPortfolioDTO = new PortfolioDTO();
        updatedPortfolioDTO.setPortfolioName("Updated Portfolio");
        updatedPortfolioDTO.setVisibility("private");

        when(portfolioRepository.findById(eq(Long.valueOf(portfolioId)))).thenReturn(Optional.of(testPortfolio));
        when(portfolioRepository.existsByPortfolioName(updatedPortfolioDTO.getPortfolioName())).thenReturn(false);

        // when
        portfolioService.updatePortfolio(portfolioId, updatedPortfolioDTO);

        // then
        verify(portfolioRepository).save(any(Portfolio.class));
        assertEquals(updatedPortfolioDTO.getPortfolioName(), testPortfolio.getPortfolioName());
        assertEquals(updatedPortfolioDTO.getVisibility(), testPortfolio.getVisibility());
    }

    @Test
    void updatePortfolio_shouldThrowException_whenPortfolioNotFound() {
        // given
        int portfolioId = 999;
        when(portfolioRepository.findById(eq(Long.valueOf(portfolioId)))).thenReturn(Optional.empty());

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> portfolioService.updatePortfolio(portfolioId, testPortfolioDTO));

        assertEquals("Portfolio not found!", exception.getMessage());
        verify(portfolioRepository, never()).save(any(Portfolio.class));
    }

    @Test
    void updatePortfolio_shouldThrowException_whenUserIsNotOwner() {
        // given
        int portfolioId = 1;
        Portfolio otherUserPortfolio = new Portfolio();
        User otherUser = new User();
        otherUser.setUsername("otheruser");
        otherUserPortfolio.setUser(otherUser);
        otherUserPortfolio.setPortfolioId(portfolioId);
        otherUserPortfolio.setPortfolioName("Other User Portfolio");

        when(portfolioRepository.findById(eq(Long.valueOf(portfolioId)))).thenReturn(Optional.of(otherUserPortfolio));

        // when & then
        SecurityException exception = assertThrows(SecurityException.class,
                () -> portfolioService.updatePortfolio(portfolioId, testPortfolioDTO));

        assertEquals("You can only update your own portfolio!", exception.getMessage());
        verify(portfolioRepository, never()).save(any(Portfolio.class));
    }

    @Test
    void updatePortfolio_shouldThrowException_whenNewNameAlreadyExists() {
        // given
        int portfolioId = 1;
        PortfolioDTO updatedPortfolioDTO = new PortfolioDTO();
        updatedPortfolioDTO.setPortfolioName("Existing Name");
        updatedPortfolioDTO.setVisibility("public");

        when(portfolioRepository.findById(eq(Long.valueOf(portfolioId)))).thenReturn(Optional.of(testPortfolio));
        when(portfolioRepository.existsByPortfolioName(updatedPortfolioDTO.getPortfolioName())).thenReturn(true);

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> portfolioService.updatePortfolio(portfolioId, updatedPortfolioDTO));

        assertEquals("Portfolio name already exists!", exception.getMessage());
        verify(portfolioRepository, never()).save(any(Portfolio.class));
    }

    @Test
    void deletePortfolio_shouldDeletePortfolio_whenPortfolioExists() {
        // given
        int portfolioId = 1;
        when(portfolioRepository.findById(eq(Long.valueOf(portfolioId)))).thenReturn(Optional.of(testPortfolio));

        // when
        portfolioService.deletePortfolio(portfolioId);

        // then
        verify(portfolioRepository).delete(testPortfolio);
    }

    @Test
    void deletePortfolio_shouldThrowException_whenPortfolioNotFound() {
        // given
        int portfolioId = 999;
        when(portfolioRepository.findById(eq(Long.valueOf(portfolioId)))).thenReturn(Optional.empty());

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> portfolioService.deletePortfolio(portfolioId));

        assertEquals("Portfolio not found!", exception.getMessage());
        verify(portfolioRepository, never()).delete(any(Portfolio.class));
    }

    @Test
    void getPortfolioById_shouldReturnPortfolio_whenPortfolioExistsAndUserIsOwner() {
        // given
        int portfolioId = 1;
        when(portfolioRepository.findById((long) portfolioId)).thenReturn(Optional.of(testPortfolio));

        // when
        PortfolioGetResponse result = portfolioService.getPortfolioById(portfolioId);

        // then
        assertNotNull(result);
        assertEquals(testPortfolio.getPortfolioId(), result.getPortfolioId());
        assertEquals(testPortfolio.getPortfolioName(), result.getPortfolioName());
        assertEquals(testPortfolio.getCreatedDate(), result.getCreatedDate());
        assertEquals(testPortfolio.getLastUpdatedDate(), result.getLastUpdatedDate());
        assertEquals(testPortfolio.getVisibility(), result.getVisibility());
        assertEquals(testPortfolio.getUser().getId(), result.getUserId());
    }

    @Test
    void getPortfolioById_shouldThrowException_whenPortfolioNotFound() {
        // given
        int portfolioId = 999;
        when(portfolioRepository.findById((long) portfolioId)).thenReturn(Optional.empty());

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> portfolioService.getPortfolioById(portfolioId));

        assertEquals("Portfolio not found or access denied", exception.getMessage());
    }

    @Test
    void getPortfolioById_shouldThrowException_whenUserIsNotOwner() {
        // given
        int portfolioId = 1;
        Portfolio otherUserPortfolio = new Portfolio();
        User otherUser = new User();
        otherUser.setId(2);
        otherUserPortfolio.setUser(otherUser);

        when(portfolioRepository.findById((long) portfolioId)).thenReturn(Optional.of(otherUserPortfolio));

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> portfolioService.getPortfolioById(portfolioId));

        assertEquals("Portfolio not found or access denied", exception.getMessage());
    }

    @Test
    void getAllPortfolios_shouldReturnAllUserPortfolios() {
        // given
        Portfolio portfolio1 = testPortfolio;

        Portfolio portfolio2 = new Portfolio();
        portfolio2.setPortfolioId(2);
        portfolio2.setPortfolioName("Second Portfolio");
        portfolio2.setVisibility("private");
        portfolio2.setCreatedDate(LocalDateTime.now(ZoneId.of("UTC")));
        portfolio2.setLastUpdatedDate(LocalDateTime.now(ZoneId.of("UTC")));
        portfolio2.setUser(testUser);

        List<Portfolio> portfolios = Arrays.asList(portfolio1, portfolio2);

        when(portfolioRepository.findByUser(testUser)).thenReturn(portfolios);

        // when
        List<PortfolioGetResponse> result = portfolioService.getAllPortfolios();

        // then
        assertEquals(2, result.size());
        assertEquals(portfolio1.getPortfolioId(), result.get(0).getPortfolioId());
        assertEquals(portfolio1.getPortfolioName(), result.get(0).getPortfolioName());
        assertEquals(portfolio2.getPortfolioId(), result.get(1).getPortfolioId());
        assertEquals(portfolio2.getPortfolioName(), result.get(1).getPortfolioName());
    }

    @Test
    void getAllPortfolios_shouldThrowException_whenUserNotFound() {
        // given
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.empty());

        // when & then
        assertThrows(UsernameNotFoundException.class, () -> portfolioService.getAllPortfolios());
    }

    @Test
    void checkPortfolioOwnership_shouldReturnTrue_whenUserOwnsPortfolio() {
        // given
        int portfolioId = 1;
        int userId = 1;
        when(portfolioRepository.findByPortfolioId(portfolioId)).thenReturn(Optional.of(testPortfolio));

        // when
        boolean result = portfolioService.checkPortfolioOwnership(portfolioId, userId);

        // then
        assertTrue(result);
    }

    @Test
    void checkPortfolioOwnership_shouldReturnFalse_whenUserDoesNotOwnPortfolio() {
        // given
        int portfolioId = 1;
        int userId = 2;
        when(portfolioRepository.findByPortfolioId(portfolioId)).thenReturn(Optional.of(testPortfolio));

        // when
        boolean result = portfolioService.checkPortfolioOwnership(portfolioId, userId);

        // then
        assertFalse(result);
    }

    @Test
    void checkPortfolioOwnership_shouldReturnFalse_whenPortfolioNotFound() {
        // given
        int portfolioId = 999;
        int userId = 1;
        when(portfolioRepository.findByPortfolioId(portfolioId)).thenReturn(Optional.empty());

        // when
        boolean result = portfolioService.checkPortfolioOwnership(portfolioId, userId);

        // then
        assertFalse(result);
    }

    @Test
    void getPortfoliosByUsername_shouldReturnAllPortfolios_whenUserRequestsOwnPortfolios() {
        // given
        Portfolio portfolio1 = testPortfolio;

        Portfolio portfolio2 = new Portfolio();
        portfolio2.setPortfolioId(2);
        portfolio2.setPortfolioName("Private Portfolio");
        portfolio2.setVisibility("private");
        portfolio2.setCreatedDate(LocalDateTime.now(ZoneId.of("UTC")));
        portfolio2.setLastUpdatedDate(LocalDateTime.now(ZoneId.of("UTC")));
        portfolio2.setUser(testUser);

        List<Portfolio> portfolios = Arrays.asList(portfolio1, portfolio2);

        when(portfolioRepository.findByUser(testUser)).thenReturn(portfolios);

        // when
        List<PortfolioGetResponse> result = portfolioService.getPortfoliosByUsername(TEST_USERNAME);

        // then
        assertEquals(2, result.size());
        assertEquals(portfolio1.getPortfolioId(), result.get(0).getPortfolioId());
        assertEquals(portfolio2.getPortfolioId(), result.get(1).getPortfolioId());
    }

    @Test
    void getPortfoliosByUsername_shouldReturnOnlyPublicPortfolios_whenOtherUserRequestsPortfolios() {
        // given
        String otherUsername = "otheruser";
        User otherUser = new User();
        otherUser.setId(2);
        otherUser.setUsername(otherUsername);

        Portfolio publicPortfolio = new Portfolio();
        publicPortfolio.setPortfolioId(1);
        publicPortfolio.setPortfolioName("Public Portfolio");
        publicPortfolio.setVisibility("public");
        publicPortfolio.setUser(otherUser);

        when(userRepository.findByUsername(otherUsername)).thenReturn(Optional.of(otherUser));
        when(portfolioRepository.findByUserAndVisibility(otherUser, "public"))
                .thenReturn(Arrays.asList(publicPortfolio));

        // when
        List<PortfolioGetResponse> result = portfolioService.getPortfoliosByUsername(otherUsername);

        // then
        assertEquals(1, result.size());
        assertEquals(publicPortfolio.getPortfolioId(), result.get(0).getPortfolioId());
        assertEquals(publicPortfolio.getPortfolioName(), result.get(0).getPortfolioName());
        assertEquals("public", result.get(0).getVisibility());
    }

    @Test
    void getPortfoliosByUsername_shouldThrowException_whenUserNotFound() {
        // given
        String nonExistentUsername = "nonexistent";
        when(userRepository.findByUsername(nonExistentUsername)).thenReturn(Optional.empty());

        // when & then
        assertThrows(UsernameNotFoundException.class,
                () -> portfolioService.getPortfoliosByUsername(nonExistentUsername));
    }
}