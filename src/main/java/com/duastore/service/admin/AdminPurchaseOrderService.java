package com.duastore.service.admin;

import com.duastore.model.PurchaseOrder;
import com.duastore.model.PurchaseOrderItem;
import com.duastore.model.Supplier;
import com.duastore.repository.PurchaseOrderRepository;
import com.duastore.repository.SupplierRepository;
import com.duastore.repository.ProductVariantRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class AdminPurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final ProductVariantRepository productVariantRepository;

    public AdminPurchaseOrderService(PurchaseOrderRepository purchaseOrderRepository,
                                      SupplierRepository supplierRepository,
                                      ProductVariantRepository productVariantRepository) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.supplierRepository = supplierRepository;
        this.productVariantRepository = productVariantRepository;
    }

    public List<PurchaseOrder> listOrders(int page, int size) {
        return purchaseOrderRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));
    }

    public List<PurchaseOrder> listByStatus(String status, int page, int size) {
        return purchaseOrderRepository.findByTrangThaiOrderByCreatedAtDesc(status, PageRequest.of(page, size));
    }

    public Optional<PurchaseOrder> findById(Integer id) {
        return purchaseOrderRepository.findById(id);
    }

    public Optional<PurchaseOrder> findByMaPhieu(String maPhieu) {
        return purchaseOrderRepository.findByMaPhieu(maPhieu);
    }

    public PurchaseOrder createOrder(PurchaseOrder order, List<PurchaseOrderItem> items) {
        String maPhieu = "PN" + System.currentTimeMillis();
        order.setMaPhieu(maPhieu);
        order.setTrangThai("CHO_DUYET");
        order.setNgayNhap(LocalDateTime.now());

        BigDecimal total = BigDecimal.ZERO;
        for (PurchaseOrderItem item : items) {
            item.setPurchaseOrder(order);
            item.setThanhTien(BigDecimal.valueOf(item.getSoLuong()).multiply(item.getGiaNhap()));
            total = total.add(item.getThanhTien());
        }
        order.setTongTien(total);
        order.getItems().addAll(items);
        return purchaseOrderRepository.save(order);
    }

    public PurchaseOrder approveOrder(Integer id, Integer approvedBy) {
        PurchaseOrder order = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu nhập"));
        if (!"CHO_DUYET".equals(order.getTrangThai())) {
            throw new RuntimeException("Chỉ duyệt được phiếu chờ duyệt");
        }
        order.setTrangThai("DA_DUYET");
        order.setApprovedBy(approvedBy);
        order.setNgayDuyet(LocalDateTime.now());
        return purchaseOrderRepository.save(order);
    }

    public PurchaseOrder receiveOrder(Integer id) {
        PurchaseOrder order = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu nhập"));
        if (!"DA_DUYET".equals(order.getTrangThai())) {
            throw new RuntimeException("Chỉ nhập hàng được phiếu đã duyệt");
        }
        for (PurchaseOrderItem item : order.getItems()) {
            if (item.getVariantId() != null) {
                productVariantRepository.findById(item.getVariantId()).ifPresent(v -> {
                    int current = v.getSoLuongTon() != null ? v.getSoLuongTon() : 0;
                    v.setSoLuongTon(current + item.getSoLuongNhan());
                    productVariantRepository.save(v);
                });
            }
        }
        order.setTrangThai("HOAN_THANH");
        order.setNgayHoanThanh(LocalDateTime.now());
        return purchaseOrderRepository.save(order);
    }

    public PurchaseOrder cancelOrder(Integer id) {
        PurchaseOrder order = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu nhập"));
        if ("HOAN_THANH".equals(order.getTrangThai())) {
            throw new RuntimeException("Không hủy được phiếu đã hoàn thành");
        }
        order.setTrangThai("DA_HUY");
        return purchaseOrderRepository.save(order);
    }

    public void deleteOrder(Integer id) {
        purchaseOrderRepository.deleteById(id);
    }

    public List<Supplier> listSuppliers() {
        return supplierRepository.findByIsActiveTrueOrderByTenNhaCungCapAsc();
    }

    public Optional<Supplier> findSupplier(Integer id) {
        return supplierRepository.findById(id);
    }

    public Supplier saveSupplier(Supplier supplier) {
        return supplierRepository.save(supplier);
    }

    public void deleteSupplier(Integer id) {
        supplierRepository.deleteById(id);
    }

    public Map<String, Long> getStatusCounts() {
        List<Object[]> rows = purchaseOrderRepository.countGroupByTrangThai();
        Map<String, Long> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            map.put((String) row[0], (Long) row[1]);
        }
        return map;
    }

    public long getTotalOrders() {
        return purchaseOrderRepository.count();
    }

    public BigDecimal getTotalImportValue(LocalDateTime start, LocalDateTime end) {
        BigDecimal val = purchaseOrderRepository.sumTongTienByNgayNhapBetween(start, end);
        return val != null ? val : BigDecimal.ZERO;
    }
}
