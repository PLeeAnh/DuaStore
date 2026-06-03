/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.duastore.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * ★ FileUploadConfig — Cấu hình upload file
 * 
 * ========== LUỒNG / HƯỚNG DẪN ==========
 * Class này cấu hình đường dẫn lưu trữ file upload (ảnh sản phẩm, avatar, v.v.)
 * và resource handler để phục vụ các file tĩnh từ thư mục upload.
 * Cần đánh dấu @Configuration và triển khai WebMvcConfigurer.
 * 
 * Nên sử dụng @Value để inject đường dẫn upload từ application.properties
 * (vd: file.upload-dir=uploads/)
 * 
 * ★ TODO [Nguyễn Văn A]: Cấu hình đường dẫn upload
 *   - Đọc cấu hình từ application.properties (file.upload-dir)
 *   - Tạo thư mục upload nếu chưa tồn tại
 *   - Đăng ký resource handler cho đường dẫn /uploads/**
 * 
 * ★ TODO [Nguyễn Văn A]: Giới hạn dung lượng file upload
 *   - Cấu hình MultipartResolver trong application.properties
 *     (spring.servlet.multipart.max-file-size, max-request-size)
 *   - Chỉ cho phép các định dạng ảnh: jpg, png, gif, webp
 * 
 * ★ TODO [Nguyễn Văn A]: Resource handler cho uploads
 *   - addResourceHandler("/uploads/**")
 *   - addResourceLocations("file:uploads/")
 *   - setCachePeriod cho caching
 * 
 * ⚠ Lưu ý: 
 *   - Đường dẫn upload phải là absolute path hoặc relative path hợp lệ
 *   - Cấu hình multipart resolver thường đặt trong application.properties
 *   - Cân nhắc sử dụng FileSystemStorageService pattern
 */
@Configuration
public class FileUploadConfig implements WebMvcConfigurer {
    
}
