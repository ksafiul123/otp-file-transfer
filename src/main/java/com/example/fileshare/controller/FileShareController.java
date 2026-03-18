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
    public ResponseEntity<UploadResponse> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(service.uploadFile(file));
    }


    @GetMapping("/validate")
    public ResponseEntity<OtpValidationResponse> validateOtp(
            @RequestParam("otp") String otp,
            HttpServletRequest request) {
        String baseUrl = request.getScheme() + "://" + request.getServerName()
                + ":" + request.getServerPort();
        return ResponseEntity.ok(service.validateOtp(otp, baseUrl));
    }


    @GetMapping("/download/{otp}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String otp) throws Exception {
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
    }
}