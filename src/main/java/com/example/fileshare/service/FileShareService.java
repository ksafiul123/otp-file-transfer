package com.example.fileshare.service;

import com.example.fileshare.dto.OtpValidationResponse;
import com.example.fileshare.dto.UploadResponse;
import com.example.fileshare.model.FileRecord;
import com.example.fileshare.repository.FileRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileShareService {

    private final FileRecordRepository repository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public UploadResponse uploadFile(MultipartFile file) throws IOException {

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("File size exceeds 5MB limit.");
        }


        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }


        String storedName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(storedName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);


        String otp = String.format("%06d", new Random().nextInt(999999));
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expires = now.plusMinutes(10);

        FileRecord record = FileRecord.builder()
                .originalFileName(file.getOriginalFilename())
                .storedFileName(storedName)
                .otp(otp)
                .uploadedAt(now)
                .expiresAt(expires)
                .used(false)
                .build();

        repository.save(record);

        return new UploadResponse(
                otp,
                file.getOriginalFilename(),
                "File uploaded successfully. Share the OTP to allow download.",
                expires.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );
    }

    public OtpValidationResponse validateOtp(String otp, String baseUrl) {
        FileRecord record = repository.findByOtp(otp)
                .orElseThrow(() -> new IllegalArgumentException("Invalid OTP."));

        if (record.isUsed()) {
            throw new IllegalStateException("OTP has already been used.");
        }
        if (LocalDateTime.now().isAfter(record.getExpiresAt())) {
            throw new IllegalStateException("OTP has expired.");
        }

        String downloadUrl = baseUrl + "/api/files/download/" + otp;

        return new OtpValidationResponse(
                downloadUrl,
                record.getOriginalFileName(),
                "OTP valid. Use the download link within the session."
        );
    }

    public Resource downloadFile(String otp) throws MalformedURLException {
        FileRecord record = repository.findByOtp(otp)
                .orElseThrow(() -> new IllegalArgumentException("Invalid OTP."));

        if (record.isUsed()) {
            throw new IllegalStateException("OTP has already been used.");
        }
        if (LocalDateTime.now().isAfter(record.getExpiresAt())) {
            throw new IllegalStateException("OTP has expired.");
        }


        record.setUsed(true);
        repository.save(record);

        Path filePath = Paths.get(uploadDir).resolve(record.getStoredFileName());
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw new RuntimeException("File not found or not readable.");
        }

        return resource;
    }
}