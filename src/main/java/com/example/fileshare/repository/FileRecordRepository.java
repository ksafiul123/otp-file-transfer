package com.example.fileshare.repository;

import com.example.fileshare.model.FileRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface FileRecordRepository extends JpaRepository<FileRecord, Long> {
    Optional<FileRecord> findByOtp(String otp);
}