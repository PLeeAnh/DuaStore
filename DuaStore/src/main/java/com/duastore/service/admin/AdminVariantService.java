/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.duastore.service.admin;

import com.duastore.model.ProductVariant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ★ AdminVariantService — Service CRUD biến thể sản phẩm cho Admin
 * 
 * ========== LUỒNG / HƯỚNG DẪN ==========
 * Service cung cấp các phương thức CRUD cho ProductVariant từ phía admin.
 * Inject ProductVariantRepository để thao tác DB.
 * 
 * Xử lý các nghiệp vụ: thêm/sửa/xóa biến thể, cập nhật tồn kho,
 * kiểm tra trùng lặp size + color trong cùng sản phẩm.
 * 
 * ★ TODO [Phạm Văn D]: CRUD cơ bản
 *   - List<ProductVariant> getVariantsByProductId(Long productId) — Lấy tất cả biến thể của sản phẩm
 *   - ProductVariant getVariantById(Long id) — Lấy biến thể theo id
 *   - ProductVariant saveVariant(ProductVariant variant) — Lưu mới hoặc cập nhật
 *   - void deleteVariant(Long id) — Xóa biến thể
 *   - void deleteAllVariantsByProductId(Long productId) — Xóa tất cả biến thể của sản phẩm
 * 
 * ★ TODO [Phạm Văn D]: Kiểm tra trùng lặp
 *   - boolean isDuplicateVariant(Long productId, String size, String color, Long excludeId)
 *     — Kiểm tra xem đã tồn tại biến thể với size + color này chưa
 *   - excludeId dùng khi sửa để loại trừ chính nó khỏi kiểm tra
 * 
 * ★ TODO [Phạm Văn D]: Quản lý tồn kho
 *   - void updateStock(Long variantId, Integer newStock) — Cập nhật số lượng tồn kho
 *   - boolean isInStock(Long variantId) — Kiểm tra còn hàng
 *   - int getTotalStockByProductId(Long productId) — Tổng tồn kho của tất cả biến thể
 * 
 * ★ TODO [Phạm Văn D]: Batch operations
 *   - List<ProductVariant> saveAllVariants(List<ProductVariant> variants)
 *     — Lưu hàng loạt (khi import từ Excel)
 *   - void updatePricesByProductId(Long productId, BigDecimal price)
 *     — Cập nhật giá đồng loạt cho tất cả biến thể của sản phẩm
 * 
 * ⚠ Lưu ý:
 *   - Đánh dấu @Service
 *   - @Transactional cho tất cả method write
 *   - Khi xóa biến thể cần kiểm tra xem có trong giỏ hàng / đơn hàng không
 *   - Không cho phép xóa biến thể cuối cùng của sản phẩm (tối thiểu 1 biến thể)
 *   - stock có thể âm? → cần validate stock >= 0
 */
@Service
@Transactional
public class AdminVariantService {
    
}
