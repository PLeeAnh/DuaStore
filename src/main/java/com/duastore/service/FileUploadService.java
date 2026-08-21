package com.duastore.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Service
/**
 * Service chứa nghiệp vụ (business logic) xử lý upload tệp/hình ảnh.
 */
public class FileUploadService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final long MAX_VIDEO_SIZE = 100 * 1024 * 1024; // 100MB
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp", "image/gif");
    private static final Set<String> ALLOWED_VIDEO_TYPES = Set.of("video/mp4", "video/quicktime", "video/webm", "video/x-msvideo");
    private static final List<byte[]> MAGIC_BYTES = Arrays.asList(
            new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}, // JPEG
            new byte[]{(byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47}, // PNG
            new byte[]{(byte) 0x52, (byte) 0x49, (byte) 0x46, (byte) 0x46}, // WEBP (RIFF...WEBP)
            new byte[]{(byte) 0x47, (byte) 0x49, (byte) 0x46, (byte) 0x38} // GIF
    );
    private static final List<byte[]> MAGIC_VIDEO_BYTES = Arrays.asList(
            new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x18, (byte) 0x66, (byte) 0x74, (byte) 0x79, (byte) 0x70}, // MP4/MOV (ISO_BMFF)
            new byte[]{(byte) 0x1A, (byte) 0x45, (byte) 0xDF, (byte) 0xA3}, // WEBM/MKV (EBML)
            new byte[]{(byte) 0x52, (byte) 0x49, (byte) 0x46, (byte) 0x46} // AVI (RIFF....AVI )
    );

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    private Path uploadPath;

    @PostConstruct
    public void init() {
        uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadPath);
        } catch (IOException ignored) {
        }
    }

    public String save(MultipartFile file) {
        return save(file, null);
    }

    public String save(MultipartFile file, String directory) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("File quá lớn, tối đa 5MB: " + file.getOriginalFilename());
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new RuntimeException("Định dạng file không được hỗ trợ: " + contentType);
        }
        if (!isValidImageContent(file)) {
            throw new RuntimeException("File không phải là ảnh hợp lệ: " + file.getOriginalFilename());
        }
        return store(file, directory);
    }

    public String saveVideo(MultipartFile file, String directory) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        if (file.getSize() > MAX_VIDEO_SIZE) {
            throw new RuntimeException("Video quá lớn, tối đa 100MB: " + file.getOriginalFilename());
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_VIDEO_TYPES.contains(contentType)) {
            throw new RuntimeException("Định dạng video không được hỗ trợ: " + contentType
                    + ". Hỗ trợ: MP4, MOV, WEBM, AVI");
        }
        String ext = detectVideoExtension(file);
        if (ext == null) {
            throw new RuntimeException("File không phải là video hợp lệ: " + file.getOriginalFilename());
        }
        return store(file, directory, ext);
    }

    private String store(MultipartFile file, String directory) {
        return store(file, directory, null);
    }

    private String store(MultipartFile file, String directory, String forcedExtension) {
        try {
            String original = file.getOriginalFilename();
            String cleanName = (original != null) ? original.replaceAll("[^a-zA-Z0-9._-]", "_") : "image";
            if (forcedExtension != null) {
                int dot = cleanName.lastIndexOf('.');
                String base = dot > 0 ? cleanName.substring(0, dot) : cleanName;
                cleanName = base + "." + forcedExtension;
            }
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

    private String detectVideoExtension(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[12];
            int read = is.read(header, 0, 12);
            if (read < 4) {
                return null;
            }
            if (startsWith(header, MAGIC_VIDEO_BYTES.get(1))) {
                return "webm";
            }
            if (startsWith(header, MAGIC_VIDEO_BYTES.get(0))) {
                return "mp4";
            }
            if (startsWith(header, MAGIC_VIDEO_BYTES.get(2))
                    && read >= 12
                    && header[8] == 'A' && header[9] == 'V' && header[10] == 'I' && header[11] == ' ') {
                return "avi";
            }
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    private boolean isValidImageContent(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[8];
            int read = is.read(header, 0, 8);
            if (read < 3) {
                return false;
            }

            for (byte[] magic : MAGIC_BYTES) {
                if (startsWith(header, magic)) {
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    private boolean startsWith(byte[] data, byte[] prefix) {
        if (data.length < prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (data[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    public boolean delete(String urlPath, String directory) {
        if (urlPath == null || directory == null || directory.isBlank()) {
            return false;
        }
        String expectedPrefix = "/uploads/" + directory + "/";
        if (!urlPath.startsWith(expectedPrefix)) {
            return false;
        }

        Path allowedDirectory = uploadPath.resolve(directory).normalize();
        Path target = allowedDirectory.resolve(urlPath.substring(expectedPrefix.length())).normalize();
        if (!target.startsWith(allowedDirectory)) {
            return false;
        }
        try {
            return Files.deleteIfExists(target);
        } catch (IOException ignored) {
            return false;
        }
    }

    public boolean delete(String urlPath) {
        if (urlPath == null || urlPath.isBlank()) {
            return false;
        }
        if (!urlPath.startsWith("/uploads/")) {
            return false;
        }
        String relative = urlPath.substring("/uploads/".length());
        Path target = uploadPath.resolve(relative).normalize();
        if (!target.startsWith(uploadPath)) {
            return false;
        }
        try {
            return Files.deleteIfExists(target);
        } catch (IOException ignored) {
            return false;
        }
    }

    public void deleteAfterCommit(String urlPath) {
        if (urlPath == null || urlPath.isBlank()) {
            return;
        }
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    delete(urlPath);
                }
            });
        } else {
            delete(urlPath);
        }
    }
}
