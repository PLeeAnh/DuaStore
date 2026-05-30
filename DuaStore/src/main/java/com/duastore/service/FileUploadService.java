package com.duastore.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;

@Service
public class FileUploadService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp", "image/gif");

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    private Path uploadPath;

    @PostConstruct
    public void init() {
        uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try { Files.createDirectories(uploadPath); } catch (IOException ignored) {}
    }

    public String save(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("File quá lớn, tối đa 5MB: " + file.getOriginalFilename());
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new RuntimeException("Định dạng file không được hỗ trợ: " + contentType);
        }

        try {
            String original = file.getOriginalFilename();
            String cleanName = (original != null) ? original.replaceAll("[^a-zA-Z0-9._-]", "_") : "image";
            String fileName = System.currentTimeMillis() + "_" + cleanName;
            Path target = uploadPath.resolve(fileName).normalize();
            if (!target.startsWith(uploadPath)) {
                throw new RuntimeException("Tên file không hợp lệ: " + original);
            }
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Không thể lưu file: " + file.getOriginalFilename(), e);
        }
    }
}
