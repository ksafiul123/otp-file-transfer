package com.example.fileshare.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OtpValidationResponse {
    private String downloadUrl;
    private String fileName;
    private String message;
}