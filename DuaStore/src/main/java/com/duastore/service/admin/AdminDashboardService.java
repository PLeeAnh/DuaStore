/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.duastore.service.admin;

import org.springframework.stereotype.Service;

/**
 * ★ AdminDashboardService — Service thống kê Dashboard Admin
 * 
 * ========== LUỒNG / HƯỚNG DẪN ==========
 * Service cung cấp dữ liệu thống kê cho trang dashboard của admin.
 * Các số liệu bao gồm: tổng sản phẩm, tổng đơn hàng, doanh thu, khách hàng, v.v.
 * 
 * Cần inject các Repository tương ứng để truy vấn dữ liệu.
 * Có thể sử dụng @Query với aggregate functions (COUNT, SUM) hoặc native queries.
 * 
 * ★ TODO [Hoàng Văn G]: Thống kê tổng quan
 *   - long getTotalProducts() — Tổng số sản phẩm (status = 1)
 *   - long getTotalOrders() — Tổng số đơn hàng
 *   - BigDecimal getTotalRevenue() — Tổng doanh thu (SUM các đơn đã hoàn thành)
 *   - long getTotalCustomers() — Tổng số khách hàng
 *   - Map<String, Long> getOrderStatusCounts() — Đếm số đơn theo trạng thái
 * 
 * ★ TODO [Hoàng Văn G]: Thống kê theo thời gian
 *   - BigDecimal getRevenueByDate(LocalDate date) — Doanh thu theo ngày
 *   - BigDecimal getRevenueByMonth(int year, int month) — Doanh thu theo tháng
 *   - BigDecimal getRevenueByYear(int year) — Doanh thu theo năm
 *   - List<Object[]> getDailyRevenueLast30Days() — Doanh thu 30 ngày gần nhất
 * 
 * ★ TODO [Hoàng Văn G]: Thống kê bổ sung
 *   - List<Product> getTopSellingProducts(int limit) — Top sản phẩm bán chạy
 *   - List<Category> getTopCategories() — Danh mục bán chạy
 *   - long getNewOrdersToday() — Đơn hàng mới hôm nay
 *   - long getPendingOrders() — Đơn hàng chưa xử lý
 * 
 * ★ TODO [Hoàng Văn G]: DTO cho Dashboard
 *   - Tạo class DashboardStats để gom các số liệu
 *   - Hoặc trả về Map<String, Object> linh hoạt
 *   - Tạo class RevenueChartData cho biểu đồ
 * 
 * ⚠ Lưu ý:
 *   - Đánh dấu @Service
 *   - Sử dụng @Transactional(readOnly = true) cho tất cả method
 *   - Các truy vấn phức tạp nên dùng native query hoặc JPQL
 *   - Cache kết quả nếu dữ liệu ít thay đổi (@Cacheable)
 *   - Cần xử lý khi chưa có đơn hàng/dữ liệu (trả về 0 hoặc BigDecimal.ZERO)
 */
@Service
public class AdminDashboardService {
    
}
