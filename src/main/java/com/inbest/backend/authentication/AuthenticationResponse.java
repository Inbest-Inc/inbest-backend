package com.inbest.backend.authentication;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Builder
@Getter
@Setter
public class AuthenticationResponse
{
    private String token;
}
