package com.inbest.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateDTO {
    @NotBlank(message = "Name cannot be blank")
    private String name;
    
    @NotBlank(message = "Surname cannot be blank")
    private String surname;
} 