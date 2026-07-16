package com.bebrample.backend.common.exception;

import lombok.*;
import org.hibernate.validator.constraints.Normalized;

import java.time.LocalDateTime;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ErrorResponse {
    private LocalDateTime timestamp = LocalDateTime.now();
    private int status;
    private String error;
    private String message;

}
