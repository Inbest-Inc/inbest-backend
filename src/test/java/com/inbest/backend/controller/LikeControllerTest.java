package com.inbest.backend.controller;

import com.inbest.backend.model.Like;
import com.inbest.backend.service.LikeService;
import jakarta.persistence.EntityNotFoundException;
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
class LikeControllerTest {

    @Mock
    LikeService mockLikeService;

    @InjectMocks
    LikeController underTest;

    @Test
    void likePost_shouldReturnOk_whenLikeIsSuccessful() {
        // given
        Long postId = 1L;
        Like mockLike = new Like();
        when(mockLikeService.likePost(postId)).thenReturn(mockLike);

        // when
        ResponseEntity<?> response = underTest.likePost(postId);

        // then
        verify(mockLikeService, times(1)).likePost(postId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("success", responseBody.get("status"));
        assertEquals("Post liked successfully!", responseBody.get("message"));
        assertEquals("1", responseBody.get("postId"));
    }

    @Test
    void likePost_shouldReturnNotFound_whenPostDoesNotExist() {
        // given
        Long postId = 1L;
        when(mockLikeService.likePost(postId))
                .thenThrow(new EntityNotFoundException("Post not found"));

        // when
        ResponseEntity<?> response = underTest.likePost(postId);

        // then
        verify(mockLikeService, times(1)).likePost(postId);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("error", responseBody.get("status"));
        assertEquals("Post not found", responseBody.get("message"));
    }

    @Test
    void likePost_shouldReturnBadRequest_whenUnexpectedExceptionOccurs() {
        // given
        Long postId = 1L;
        when(mockLikeService.likePost(postId))
                .thenThrow(new RuntimeException("Unexpected error"));

        // when
        ResponseEntity<?> response = underTest.likePost(postId);

        // then
        verify(mockLikeService, times(1)).likePost(postId);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("error", responseBody.get("status"));
        assertEquals("Unexpected error", responseBody.get("message"));
    }

    @Test
    void unlikePost_shouldReturnOk_whenUnlikeIsSuccessful() {
        // given
        Long postId = 1L;
        doNothing().when(mockLikeService).unlikePost(postId);

        // when
        ResponseEntity<?> response = underTest.unlikePost(postId);

        // then
        verify(mockLikeService, times(1)).unlikePost(postId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("success", responseBody.get("status"));
        assertEquals("Post unliked successfully!", responseBody.get("message"));
        assertEquals("1", responseBody.get("postId"));
    }

    @Test
    void unlikePost_shouldReturnNotFound_whenPostDoesNotExist() {
        // given
        Long postId = 1L;
        doThrow(new EntityNotFoundException("Post not found"))
                .when(mockLikeService)
                .unlikePost(postId);

        // when
        ResponseEntity<?> response = underTest.unlikePost(postId);

        // then
        verify(mockLikeService, times(1)).unlikePost(postId);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("error", responseBody.get("status"));
        assertEquals("Post not found", responseBody.get("message"));
    }

    @Test
    void unlikePost_shouldReturnBadRequest_whenUnexpectedExceptionOccurs() {
        // given
        Long postId = 1L;
        doThrow(new RuntimeException("Unexpected error"))
                .when(mockLikeService)
                .unlikePost(postId);

        // when
        ResponseEntity<?> response = underTest.unlikePost(postId);

        // then
        verify(mockLikeService, times(1)).unlikePost(postId);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("error", responseBody.get("status"));
        assertEquals("Unexpected error", responseBody.get("message"));
    }

    @Test
    void getLikeCount_shouldReturnOkWithCount_whenPostExists() {
        // given
        Long postId = 1L;
        when(mockLikeService.getLikeCount(postId)).thenReturn(5L);

        // when
        ResponseEntity<?> response = underTest.getLikeCount(postId);

        // then
        verify(mockLikeService, times(1)).getLikeCount(postId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("success", responseBody.get("status"));
        assertEquals(postId, responseBody.get("postId"));
        assertEquals(5L, responseBody.get("likeCount"));
    }

    @Test
    void getLikeCount_shouldReturnNotFound_whenPostDoesNotExist() {
        // given
        Long postId = 1L;
        when(mockLikeService.getLikeCount(postId))
                .thenThrow(new EntityNotFoundException("Post not found"));

        // when
        ResponseEntity<?> response = underTest.getLikeCount(postId);

        // then
        verify(mockLikeService, times(1)).getLikeCount(postId);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("error", responseBody.get("status"));
        assertEquals("Post not found", responseBody.get("message"));
    }

    @Test
    void getLikeCount_shouldReturnBadRequest_whenUnexpectedExceptionOccurs() {
        // given
        Long postId = 1L;
        when(mockLikeService.getLikeCount(postId))
                .thenThrow(new RuntimeException("Unexpected error"));

        // when
        ResponseEntity<?> response = underTest.getLikeCount(postId);

        // then
        verify(mockLikeService, times(1)).getLikeCount(postId);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("error", responseBody.get("status"));
        assertEquals("Unexpected error", responseBody.get("message"));
    }

    @Test
    void hasUserLikedPost_shouldReturnTrue_whenUserHasLiked() {
        // given
        Long postId = 1L;
        when(mockLikeService.hasUserLikedPost(postId)).thenReturn(true);

        // when
        ResponseEntity<?> response = underTest.hasUserLikedPost(postId);

        // then
        verify(mockLikeService, times(1)).hasUserLikedPost(postId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("success", responseBody.get("status"));
        assertEquals(postId, responseBody.get("postId"));
        assertTrue((Boolean) responseBody.get("hasLiked"));
    }

    @Test
    void hasUserLikedPost_shouldReturnFalse_whenUserHasNotLiked() {
        // given
        Long postId = 1L;
        when(mockLikeService.hasUserLikedPost(postId)).thenReturn(false);

        // when
        ResponseEntity<?> response = underTest.hasUserLikedPost(postId);

        // then
        verify(mockLikeService, times(1)).hasUserLikedPost(postId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("success", responseBody.get("status"));
        assertEquals(postId, responseBody.get("postId"));
        assertFalse((Boolean) responseBody.get("hasLiked"));
    }

    @Test
    void hasUserLikedPost_shouldReturnNotFound_whenPostDoesNotExist() {
        // given
        Long postId = 1L;
        when(mockLikeService.hasUserLikedPost(postId))
                .thenThrow(new EntityNotFoundException("Post not found"));

        // when
        ResponseEntity<?> response = underTest.hasUserLikedPost(postId);

        // then
        verify(mockLikeService, times(1)).hasUserLikedPost(postId);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("error", responseBody.get("status"));
        assertEquals("Post not found", responseBody.get("message"));
    }

    @Test
    void hasUserLikedPost_shouldReturnBadRequest_whenUnexpectedExceptionOccurs() {
        // given
        Long postId = 1L;
        when(mockLikeService.hasUserLikedPost(postId))
                .thenThrow(new RuntimeException("Unexpected error"));

        // when
        ResponseEntity<?> response = underTest.hasUserLikedPost(postId);

        // then
        verify(mockLikeService, times(1)).hasUserLikedPost(postId);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("error", responseBody.get("status"));
        assertEquals("Unexpected error", responseBody.get("message"));
    }
}
