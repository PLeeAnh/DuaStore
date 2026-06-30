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
        return save(file, null);
    }

    public String save(MultipartFile file, String directory) {
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

            Path dirPath = (directory != null && !directory.isBlank())
                ? uploadPath.resolve(directory).normalize()
                : uploadPath;
            Files.createDirectories(dirPath);

            Path target = dirPath.resolve(fileName).normalize();
            if (!target.startsWith(dirPath)) {
                throw new RuntimeException("Tên file không hợp lệ: " + original);
            }
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            String urlPath = (directory != null && !directory.isBlank())
                ? "/uploads/" + directory + "/" + fileName
                : "/uploads/" + fileName;
            return urlPath;
        } catch (IOException e) {
            throw new RuntimeException("Không thể lưu file: " + file.getOriginalFilename(), e);
        }
    }

    public boolean delete(String urlPath, String directory) {
        if (urlPath == null || directory == null || directory.isBlank()) return false;
        String expectedPrefix = "/uploads/" + directory + "/";
        if (!urlPath.startsWith(expectedPrefix)) return false;

        Path allowedDirectory = uploadPath.resolve(directory).normalize();
        Path target = allowedDirectory.resolve(urlPath.substring(expectedPrefix.length())).normalize();
        if (!target.startsWith(allowedDirectory)) return false;
        try {
            return Files.deleteIfExists(target);
        } catch (IOException ignored) {
            return false;
        }
    }
}
