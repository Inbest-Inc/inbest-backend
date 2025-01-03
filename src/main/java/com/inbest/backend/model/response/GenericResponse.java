package com.inbest.backend.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GenericResponse {
    private String status;
    private String message;
    private Object data;
}
