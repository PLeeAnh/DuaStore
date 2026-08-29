package com.duastore.service.admin;

import com.duastore.model.ProductVariant;
import com.duastore.model.StockMovement;
import com.duastore.repository.ProductVariantRepository;
import com.duastore.repository.StockMovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service xử lý nghiệp vụ xuất nhập kho.
 * Ghi nhận mỗi lần thay đổi tồn kho variant để phục vụ kiểm kê và đối soát.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class StockMovementService {

    private final StockMovementRepository stockMovementRepository;
    private final ProductVariantRepository productVariantRepository;

    /** Tạo bản ghi xuất nhập kho khi thay đổi tồn kho */
    public StockMovement record(Integer variantId, Integer quantity, String type,
                                 Integer orderId, Integer userId, String note) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể #" + variantId));

        int stockBefore = variant.getSoLuongTon() != null ? variant.getSoLuongTon() : 0;
        int stockAfter = stockBefore + quantity;

        StockMovement movement = StockMovement.builder()
                .variantId(variantId)
                .quantity(quantity)
                .type(type)
                .orderId(orderId)
                .userId(userId)
                .note(note)
                .stockBefore(stockBefore)
                .stockAfter(stockAfter)
                .build();

        return stockMovementRepository.save(movement);
    }

    /** Ghi nhận nhập kho */
    public StockMovement recordIn(Integer variantId, int quantity, Integer userId, String note) {
        return record(variantId, quantity, "IN", null, userId, note);
    }

    /** Ghi nhận xuất kho (bán/huỷ đơn) */
    public StockMovement recordOut(Integer variantId, int quantity, Integer orderId, Integer userId, String note) {
        return record(variantId, -quantity, "OUT", orderId, userId, note);
    }

    /** Ghi nhận điều chỉnh tồn kho thủ công */
    public StockMovement recordAdjust(Integer variantId, int oldStock, int newStock, Integer userId, String note) {
        int diff = newStock - oldStock;
        return record(variantId, diff, "ADJUST", null, userId, note);
    }

    /** Lấy lịch sử theo biến thể, phân trang */
    @Transactional(readOnly = true)
    public Page<StockMovement> getByVariant(Integer variantId, Pageable pageable) {
        return stockMovementRepository.findByVariantIdOrderByCreatedAtDesc(variantId, pageable);
    }

    /** Lấy lịch sử theo biến thể, không phân trang */
    @Transactional(readOnly = true)
    public List<StockMovement> getByVariant(Integer variantId) {
        return stockMovementRepository.findByVariantIdOrderByCreatedAtDesc(variantId);
    }

    /** Lấy lịch sử toàn bộ, phân trang */
    @Transactional(readOnly = true)
    public Page<StockMovement> getAll(Pageable pageable) {
        return stockMovementRepository.findAll(pageable);
    }

    /** Tổng nhập xuất trong khoảng ngày */
    @Transactional(readOnly = true)
    public long[] getSummary(Integer variantId, int days) {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(days);
        long totalIn = stockMovementRepository.sumInByVariantAndDateRange(variantId, start, end);
        long totalOut = stockMovementRepository.sumOutByVariantAndDateRange(variantId, start, end);
        return new long[]{totalIn, totalOut};
    }
}
