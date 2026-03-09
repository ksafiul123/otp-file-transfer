package com.example.fileshare.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String originalFileName;
    private String storedFileName;
    private String otp;
    private LocalDateTime uploadedAt;
    private LocalDateTime expiresAt;
    private boolean used;
}