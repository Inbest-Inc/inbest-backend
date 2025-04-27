package com.inbest.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jdk.jshell.Snippet;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserDTO
{
    private String username;
    @JsonIgnore
    private String email;
    private String name;
    private String surname;
    private String image_url;
    private Long followerCount;
}
