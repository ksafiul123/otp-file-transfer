package com.example.fileshare.controller;

import com.example.fileshare.dto.OtpValidationResponse;
import com.example.fileshare.dto.UploadResponse;
import com.example.fileshare.service.FileShareService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileShareController {

    private final FileShareService service;


    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            UploadResponse response = service.uploadFile(file);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("File upload failed: " + e.getMessage());
        }
    }


    @GetMapping("/validate")
    public ResponseEntity<?> validateOtp(
            @RequestParam("otp") String otp,
            HttpServletRequest request) {
        try {
            String baseUrl = request.getScheme() + "://" + request.getServerName()
                    + ":" + request.getServerPort();
            OtpValidationResponse response = service.validateOtp(otp, baseUrl);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @GetMapping("/download/{otp}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String otp) {
        try {
            Resource resource = service.downloadFile(otp);
            String filename = resource.getFilename();

            if (filename != null && filename.contains("_")) {
                filename = filename.substring(filename.indexOf("_") + 1);
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}