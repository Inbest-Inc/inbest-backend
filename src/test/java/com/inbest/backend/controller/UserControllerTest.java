package com.inbest.backend.controller;

import com.inbest.backend.dto.UserUpdateDTO;
import com.inbest.backend.exception.UserNotFoundException;
import com.inbest.backend.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    UserService mockUserService;

    @InjectMocks
    UserController underTest;

    @Test
    void getPublicUserInfo_shouldReturnOk_whenUserExists() {
        // given
        String username = "john_doe";
        when(mockUserService.getPublicUserInfo(username)).thenReturn("John Doe");

        // when
        ResponseEntity<?> response = underTest.getPublicUserInfo(username);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("John Doe", responseBody.get("name"));
        verify(mockUserService, times(1)).getPublicUserInfo(username);
    }


    @Test
    void updateUser_shouldReturnOk_whenUpdateIsSuccessful() {
        UserUpdateDTO userUpdateDTO = new UserUpdateDTO();
        userUpdateDTO.setName("John");
        userUpdateDTO.setSurname("Doe");

        ResponseEntity<?> response = underTest.updateUserNameAndSurname(userUpdateDTO);

        verify(mockUserService, times(1)).updateUserNameAndSurname(userUpdateDTO);
        assertEquals(200, response.getStatusCodeValue());

        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("User information updated successfully", responseBody.get("message"));

    }

    @Test
    void updateUser_shouldReturnNotFound_whenUserNotFoundExceptionIsThrown() {
        // given
        UserUpdateDTO userUpdateDTO = new UserUpdateDTO();

        doThrow(new UserNotFoundException("User not found"))
                .when(mockUserService)
                .updateUserNameAndSurname(userUpdateDTO);

        // when
        ResponseEntity<?> response = underTest.updateUserNameAndSurname(userUpdateDTO);

        // then
        verify(mockUserService, times(1)).updateUserNameAndSurname(userUpdateDTO);
        assertEquals(404, response.getStatusCodeValue());
        Map<String, String> responseBody = (Map<String, String>) response.getBody();

        assertEquals("User not found", responseBody.get("error"));
    }
    @Test
    void updateUser_shouldReturnInternalServerError_whenUnexpectedExceptionIsThrown() {
        // given
        UserUpdateDTO userUpdateDTO = new UserUpdateDTO();

        doThrow(new RuntimeException("Unexpected error"))
                .when(mockUserService)
                .updateUserNameAndSurname(userUpdateDTO);

        // when
        ResponseEntity<?> response = underTest.updateUserNameAndSurname(userUpdateDTO);

        // then
        verify(mockUserService, times(1)).updateUserNameAndSurname(userUpdateDTO);
        assertEquals(500, response.getStatusCodeValue());

        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("An error occurred while updating user information", responseBody.get("error"));
    }

    @Test
    void updateUser_shouldReturnInternalServerError_whenUnexpectedErrorOccurs() {
        // given
        UserUpdateDTO userUpdateDTO = new UserUpdateDTO();

        doThrow(new RuntimeException("Unexpected error"))
                .when(mockUserService).updateUserNameAndSurname(userUpdateDTO);

        // when
        ResponseEntity<?> response = underTest.updateUserNameAndSurname(userUpdateDTO);

        // then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("An error occurred while updating user information", responseBody.get("error"));
    }

     /*@Test
    void updateUser_shouldReturnBadRequest_whenInvalidUserUpdateDTO() {
        // given
        UserUpdateDTO invalidUserUpdateDTO = new UserUpdateDTO(); // Boş değerler geçersiz kabul edilir
        // when
        ResponseEntity<?> response = underTest.updateUser(invalidUserUpdateDTO);

        // then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void updateUser_shouldReturnBadRequest_whenSurnameIsTooLong() {
        // given
        UserUpdateDTO invalidDTO = new UserUpdateDTO();
        invalidDTO.setName("John");
        invalidDTO.setSurname("A".repeat(256));  // 256 karakter uzunluk

        // when
        ResponseEntity<?> response = underTest.updateUser(invalidDTO);

        // then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Surname is too long"));
    }*/
}