package com.inbest.backend.service;

import com.inbest.backend.dto.UserUpdateDTO;
import com.inbest.backend.exception.UserNotFoundException;
import com.inbest.backend.model.User;
import com.inbest.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest {

    @Mock
    private UserRepository repository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // SecurityContext ayarı
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void getPublicUserInfo() {
    }

    @Test
    void updateUserNameAndSurname_shouldUpdateUser_whenUserExists() {
        // given
        String username = "johndoe";
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setName("UpdatedName");
        dto.setSurname("UpdatedSurname");

        User user = new User();
        user.setUsername(username);

        when(authentication.getName()).thenReturn(username);
        when(repository.findByUsername(username)).thenReturn(Optional.of(user));

        // when
        userService.updateUserNameAndSurname(dto);

        // then
        assertEquals("UpdatedName", user.getName());
        assertEquals("UpdatedSurname", user.getSurname());
        verify(repository).save(user);
    }

    @Test
    void updateUserNameAndSurname_shouldThrowUserNotFoundException_whenUserDoesNotExist() {
        // given
        String username = "nonexistentuser";
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setName("Name");
        dto.setSurname("Surname");

        when(authentication.getName()).thenReturn(username);
        when(repository.findByUsername(username)).thenReturn(Optional.empty());

        // when & then
        assertThrows(UserNotFoundException.class, () -> userService.updateUserNameAndSurname(dto));
    }

}