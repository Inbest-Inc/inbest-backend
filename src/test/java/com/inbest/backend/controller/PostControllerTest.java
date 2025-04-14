package com.inbest.backend.controller;

import com.inbest.backend.dto.PostCreateDTO;
import com.inbest.backend.dto.PostResponseDTO;
import com.inbest.backend.exception.UserNotFoundException;
import com.inbest.backend.service.PostService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostControllerTest {

    @Mock
    PostService mockPostService;

    @InjectMocks
    PostController underTest;

    @Test
    void createPost_shouldReturnOk_whenPostIsCreatedSuccessfully() {
        // given
        PostCreateDTO postDTO = new PostCreateDTO();
        postDTO.setContent("Test content");
        postDTO.setInvestmentActivityId(1L);

        PostResponseDTO mockResponse = new PostResponseDTO();
        when(mockPostService.createPost(postDTO)).thenReturn(mockResponse);

        // when
        ResponseEntity<?> response = underTest.createPost(postDTO);

        // then
        verify(mockPostService, times(1)).createPost(postDTO);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("success", responseBody.get("status"));
        assertEquals("Post created successfully", responseBody.get("message"));
        assertEquals(mockResponse, responseBody.get("post"));
    }


    @Test
    void createPost_shouldReturnBadRequest_whenInvestmentActivityNotFound() {
        // given
        PostCreateDTO postDTO = new PostCreateDTO();
        postDTO.setContent("Test content");
        postDTO.setInvestmentActivityId(999L); // Non-existent investment activity ID

        when(mockPostService.createPost(postDTO))
                .thenThrow(new IllegalStateException("Investment activity not found"));

        // when
        ResponseEntity<?> response = underTest.createPost(postDTO);

        // then
        verify(mockPostService, times(1)).createPost(postDTO);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("error", responseBody.get("status"));
        assertEquals("Investment activity not found", responseBody.get("message"));
    }

    @Test
    void createPost_shouldReturnBadRequest_whenIllegalStateExceptionThrown() {
        // given
        PostCreateDTO postDTO = new PostCreateDTO();
        postDTO.setContent("Test content");
        postDTO.setInvestmentActivityId(1L);

        when(mockPostService.createPost(postDTO))
                .thenThrow(new IllegalStateException("Invalid investment activity"));

        // when
        ResponseEntity<?> response = underTest.createPost(postDTO);

        // then
        verify(mockPostService, times(1)).createPost(postDTO);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("error", responseBody.get("status"));
        assertEquals("Invalid investment activity", responseBody.get("message"));
    }

    @Test
    void getAllPosts_shouldReturnOk_whenPostsExist() {
        // given
        List<PostResponseDTO> mockPosts = Arrays.asList(new PostResponseDTO(), new PostResponseDTO());
        when(mockPostService.getAllPosts()).thenReturn(mockPosts);

        // when
        ResponseEntity<?> response = underTest.getAllPosts();

        // then
        verify(mockPostService, times(1)).getAllPosts();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("success", responseBody.get("status"));
        assertEquals("Posts found!", responseBody.get("message"));
        assertEquals(mockPosts, responseBody.get("data"));

    }

    @Test
    void getAllPosts_shouldReturnNotFound_whenNoPostsExist() {
        // given
        when(mockPostService.getAllPosts()).thenReturn(Collections.emptyList());

        // when
        ResponseEntity<?> response = underTest.getAllPosts();

        // then
        verify(mockPostService, times(1)).getAllPosts();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("success", responseBody.get("status"));
        assertEquals("User do not have any posts", responseBody.get("message"));
    }

    @Test
    void getAllPosts_shouldReturnServerError_whenDatabaseErrorOccurs() {
        // given
        when(mockPostService.getAllPosts())
                .thenThrow(new DataAccessException("Database error") {});

        // when
        ResponseEntity<?> response = underTest.getAllPosts();

        // then
        verify(mockPostService, times(1)).getAllPosts();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("error", responseBody.get("status"));
        assertEquals("An error occurred while accessing the database. Please try again later.", responseBody.get("message"));
    }

    @Test
    void getPostById_shouldReturnOk_whenPostExists() {
        // given
        Long postId = 1L;
        PostResponseDTO mockPost = new PostResponseDTO();
        when(mockPostService.getPostById(postId)).thenReturn(Optional.of(mockPost));

        // when
        ResponseEntity<?> response = underTest.getPostById(postId);

        // then
        verify(mockPostService, times(1)).getPostById(postId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("success", responseBody.get("status"));
        assertEquals("Posts found!", responseBody.get("message"));
        assertEquals(mockPost, responseBody.get("data"));

    }

    @Test
    void getPostById_shouldReturnNotFound_whenPostDoesNotExist() {
        // given
        Long postId = 1L;
        when(mockPostService.getPostById(postId)).thenReturn(Optional.empty());

        // when
        ResponseEntity<?> response = underTest.getPostById(postId);

        // then
        verify(mockPostService, times(1)).getPostById(postId);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("error", responseBody.get("status"));
        assertEquals("Post not found", responseBody.get("message"));
    }

    @Test
    void getPostById_shouldReturnServerError_whenDatabaseErrorOccurs() {
        // given
        Long postId = 1L;
        when(mockPostService.getPostById(postId))
                .thenThrow(new DataAccessException("Database error") {});

        // when
        ResponseEntity<?> response = underTest.getPostById(postId);

        // then
        verify(mockPostService, times(1)).getPostById(postId);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("error", responseBody.get("status"));
        assertEquals("An error occurred while accessing the database. Please try again later.", responseBody.get("message"));
    }

    @Test
    void getPostsByUsername_shouldReturnOk_whenPostsExistForUser() {
        // given
        String username = "testuser";
        List<PostResponseDTO> mockPosts = Arrays.asList(new PostResponseDTO(), new PostResponseDTO());
        when(mockPostService.getPostsByUsername(username)).thenReturn(mockPosts);

        // when
        ResponseEntity<?> response = underTest.getPostsByUsername(username);

        // then
        verify(mockPostService, times(1)).getPostsByUsername(username);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("success", responseBody.get("status"));
        assertEquals("Posts found!", responseBody.get("message"));
        assertEquals(mockPosts, responseBody.get("data"));
    }

    @Test
    void getPostsByUsername_shouldReturnNotFound_whenNoPostsExistForUser() {
        // given
        String username = "testuser";
        when(mockPostService.getPostsByUsername(username)).thenReturn(Collections.emptyList());

        // when
        ResponseEntity<?> response = underTest.getPostsByUsername(username);

        // then
        verify(mockPostService, times(1)).getPostsByUsername(username);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("error", responseBody.get("status"));
        assertEquals("No posts found for user: " + username, responseBody.get("message"));
    }

    @Test
    void getPostsByUsername_shouldReturnNotFound_whenUserDoesNotExist() {
        // given
        String username = "testuser";
        when(mockPostService.getPostsByUsername(username))
                .thenThrow(new UserNotFoundException("User not found"));

        // when
        ResponseEntity<?> response = underTest.getPostsByUsername(username);

        // then
        verify(mockPostService, times(1)).getPostsByUsername(username);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("error", responseBody.get("status"));
        assertEquals("User not found", responseBody.get("message"));
    }

    @Test
    void getPostsByUsername_shouldReturnServerError_whenDatabaseErrorOccurs() {
        // given
        String username = "testuser";
        when(mockPostService.getPostsByUsername(username))
                .thenThrow(new DataAccessException("Database error") {});

        // when
        ResponseEntity<?> response = underTest.getPostsByUsername(username);

        // then
        verify(mockPostService, times(1)).getPostsByUsername(username);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("error", responseBody.get("status"));
        assertEquals("An error occurred while accessing the database. Please try again later.", responseBody.get("message"));
    }

    @Test
    void deletePost_shouldReturnOk_whenPostDeletedSuccessfully() {
        // given
        Long postId = 1L;
        doNothing().when(mockPostService).deletePost(postId);

        // when
        ResponseEntity<?> response = underTest.deletePost(postId);

        // then
        verify(mockPostService, times(1)).deletePost(postId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("success", responseBody.get("status"));
        assertEquals("Post deleted successfully", responseBody.get("message"));
    }

    @Test
    void deletePost_shouldReturnNotFound_whenIllegalStateExceptionThrown() {
        // given
        Long postId = 1L;
        doThrow(new IllegalStateException("Post not found"))
                .when(mockPostService)
                .deletePost(postId);

        // when
        ResponseEntity<?> response = underTest.deletePost(postId);

        // then
        verify(mockPostService, times(1)).deletePost(postId);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("error", responseBody.get("status"));
        assertEquals("Post not found", responseBody.get("message"));
    }

    @Test
    void deletePost_shouldReturnServerError_whenDatabaseErrorOccurs() {
        // given
        Long postId = 1L;
        doThrow(new DataAccessException("Database error") {})
                .when(mockPostService)
                .deletePost(postId);

        // when
        ResponseEntity<?> response = underTest.deletePost(postId);

        // then
        verify(mockPostService, times(1)).deletePost(postId);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("error", responseBody.get("status"));
        assertEquals("An error occurred while accessing the database. Please try again later.", responseBody.get("message"));
    }
}

