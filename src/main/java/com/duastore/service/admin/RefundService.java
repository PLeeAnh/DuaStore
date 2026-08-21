package com.duastore.service.admin;

import com.duastore.model.Order;
import com.duastore.model.OrderEventType;
import com.duastore.model.OrderItem;
import com.duastore.model.ProductVariant;
import com.duastore.model.RefundRequest;
import com.duastore.model.RefundReason;
import com.duastore.model.RefundType;
import com.duastore.model.ReturnCondition;
import com.duastore.model.User;
import com.duastore.repository.OrderRepository;
import com.duastore.repository.ProductVariantRepository;
import com.duastore.repository.RefundRequestRepository;
import com.duastore.repository.UserRepository;
import com.duastore.service.EmailService;
import com.duastore.service.LoyaltyPointsService;
import com.duastore.service.RefundPolicyService;
import com.duastore.service.SiteSettingService;
import com.duastore.service.VNPAYService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
/**
 * Service chứa nghiệp vụ (business logic) xử lý hoàn trả/đổi trả đơn hàng.
 */
public class RefundService {

    private static final Logger log = LoggerFactory.getLogger(RefundService.class);

    private final RefundRequestRepository refundRequestRepository;
    private final OrderRepository orderRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;
    private final OrderStatusLogService orderStatusLogService;
    private final LoyaltyPointsService loyaltyPointsService;
    private final RefundPolicyService refundPolicyService;
    private final EmailService emailService;
    private final SiteSettingService siteSettingService;
    private final VNPAYService vnpayService;

    public RefundService(RefundRequestRepository refundRequestRepository,
            OrderRepository orderRepository,
            ProductVariantRepository productVariantRepository,
            UserRepository userRepository,
            OrderStatusLogService orderStatusLogService,
            LoyaltyPointsService loyaltyPointsService,
            RefundPolicyService refundPolicyService,
            EmailService emailService,
            SiteSettingService siteSettingService,
            VNPAYService vnpayService) {
        this.refundRequestRepository = refundRequestRepository;
        this.orderRepository = orderRepository;
        this.productVariantRepository = productVariantRepository;
        this.userRepository = userRepository;
        this.orderStatusLogService = orderStatusLogService;
        this.loyaltyPointsService = loyaltyPointsService;
        this.refundPolicyService = refundPolicyService;
        this.emailService = emailService;
        this.siteSettingService = siteSettingService;
        this.vnpayService = vnpayService;
    }

    @Transactional(readOnly = true)
    public List<RefundRequest> getAll() {
        return refundRequestRepository.findAllByOrderByNgayYeuCauDesc();
    }

    @Transactional(readOnly = true)
    public RefundRequest getById(Integer id) {
        return refundRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu hoàn tiền"));
    }

    @Transactional(readOnly = true)
    public boolean hasRefundRequestByOrderId(Integer orderId) {
        return refundRequestRepository.existsByOrderId(orderId);
    }

    @Transactional(readOnly = true)
    public Optional<RefundRequest> getActiveRefundByOrderId(Integer orderId) {
        return refundRequestRepository.findActiveRefundByOrderId(orderId);
    }

    public RefundRequest create(RefundRequest request) {
        return refundRequestRepository.save(request);
    }

    public RefundRequest submitRefundRequest(RefundRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (!refundPolicyService.isRefundableStatus(order.getTrangThaiDon())) {
            throw new RuntimeException("Chỉ có thể yêu cầu hoàn tiền cho đơn hàng đã giao");
        }

        RefundType type = RefundType.fromCode(request.getLoaiYeuCau());
        RefundReason reason = RefundReason.fromCode(request.getLyDoChiTiet());

        if (!refundPolicyService.validateRefundEligibility(order, type, reason)) {
            throw new RuntimeException("Đơn hàng không đủ điều kiện " + type.getDisplayName().toLowerCase());
        }

        for (OrderItem item : order.getOrderItems()) {
            if (item.getVariantId() != null) {
                ProductVariant variant = productVariantRepository.findById(item.getVariantId()).orElse(null);
                if (variant != null && variant.isCustom()) {
                    if (type == RefundType.HOAN_TIEN && refundPolicyService.isCustomGlassNonRefundable()) {
                        throw new RuntimeException("Sản phẩm custom (đặt riêng) không hỗ trợ hoàn tiền, chỉ được đổi size/màu");
                    }
                }
            }
        }

        request.setTrangThaiDonHangKhiYeuCau(order.getTrangThaiDon());
        request.setNgayYeuCau(LocalDateTime.now());
        request.setTrangThai("CHO_DUYET");

        if (reason.requiresVideoProof() && (request.getVideoUnboxing() == null || request.getVideoUnboxing().isBlank())) {
            throw new RuntimeException("Yêu cầu video unboxing cho lý do: " + reason.getDisplayName());
        }

        BigDecimal shippingFee = order.getPhiVanChuyen();
        BigDecimal refundAmount = refundPolicyService.calculateRefundAmount(order, type, reason, shippingFee);
        request.setSoTienHoan(refundAmount);

        if (type == RefundType.HOAN_TIEN) {
            if (reason.isShopFault()) {
                request.setPhiShipTraLai(shippingFee);
            } else {
                request.setPhiShipTraLai(BigDecimal.ZERO);
            }
        }

        RefundRequest saved = refundRequestRepository.save(request);

        orderRepository.findById(request.getOrderId()).ifPresent(o -> {
            o.setTrangThaiDon("DA_YEU_CAU_HOAN_TIEN");
            orderRepository.save(o);
        });

        return saved;
    }

    public RefundRequest approve(Integer id, Integer adminId, String ghiChu) {
        RefundRequest request = getById(id);
        if (!"CHO_DUYET".equals(request.getTrangThai())) {
            throw new RuntimeException("Yêu cầu hoàn tiền đã được xử lý");
        }

        request.setTrangThai("DA_DUYET");
        request.setNguoiXuLyId(adminId);
        request.setNgayXuLy(LocalDateTime.now());
        request.setGhiChuXuLy(ghiChu);
        refundRequestRepository.save(request);

        orderRepository.findById(request.getOrderId()).ifPresent(order -> {
            String oldStatus = order.getTrangThaiDon();
            if ("DA_YEU_CAU_HOAN_TIEN".equals(oldStatus) || "DA_GIAO".equals(oldStatus) || "DA_HOAN_THANH".equals(oldStatus)) {
                order.setTrangThaiDon("DANG_TRA_HANG");
                orderRepository.save(order);
                User admin = userRepository.findById(adminId).orElse(null);
                orderStatusLogService.ghiLog(order, OrderEventType.STATUS_CHANGE, admin,
                        oldStatus, "DANG_TRA_HANG", "Duyệt yêu cầu: " + ghiChu);
            }
        });

        // Send email notification
        sendRefundApprovedEmail(request);

        return request;
    }

    public RefundRequest reject(Integer id, Integer adminId, String ghiChu) {
        RefundRequest request = getById(id);
        if (!"CHO_DUYET".equals(request.getTrangThai())) {
            throw new RuntimeException("Yêu cầu hoàn tiền đã được xử lý");
        }

        request.setTrangThai("TU_CHOI");
        request.setNguoiXuLyId(adminId);
        request.setNgayXuLy(LocalDateTime.now());
        request.setGhiChuXuLy(ghiChu);
        refundRequestRepository.save(request);

        orderRepository.findById(request.getOrderId()).ifPresent(order -> {
            String oldStatus = order.getTrangThaiDon();
            if ("DA_YEU_CAU_HOAN_TIEN".equals(oldStatus)) {
                order.setTrangThaiDon("TU_CHOI_HOAN_TIEN");
                orderRepository.save(order);
                User admin = userRepository.findById(adminId).orElse(null);
                orderStatusLogService.ghiLog(order, OrderEventType.STATUS_CHANGE, admin,
                        oldStatus, "TU_CHOI_HOAN_TIEN", "Từ chối: " + ghiChu);
            }
        });

        return request;
    }

    public RefundRequest processWarehouseInspection(Integer id, ReturnCondition condition, Integer adminId, String ghiChu) {
        RefundRequest request = getById(id);
        if (!"DA_DUYET".equals(request.getTrangThai()) && !"DANG_TRA_HANG".equals(request.getTrangThai())) {
            throw new RuntimeException("Yêu cầu không ở trạng thái chờ kiểm tra kho");
        }

        request.setDaKiemTraHang(true);
        request.setTinhTrangHangTra(condition.name());
        request.setNgayNhanHangTra(LocalDateTime.now());
        request.setGhiChuXuLy(ghiChu);
        refundRequestRepository.save(request);

        orderRepository.findById(request.getOrderId()).ifPresent(order -> {
            order.setTrangThaiDon("DA_NHAN_HANG_TRA");
            orderRepository.save(order);
            User admin = userRepository.findById(adminId).orElse(null);
            orderStatusLogService.ghiLog(order, OrderEventType.STATUS_CHANGE, admin,
                    "DANG_TRA_HANG", "DA_NHAN_HANG_TRA", "Kho kiểm tra: " + condition.getDisplayName() + ". " + ghiChu);
        });

        // Send email notification - warehouse received
        sendWarehouseReceivedEmail(request);

        boolean isRefund = RefundType.HOAN_TIEN.name().equals(request.getLoaiYeuCau());

        if (condition == ReturnCondition.NGUYEN_VINH) {
            if (isRefund) {
                return completeRefund(id, adminId, "Hàng nguyên vẹn - Hoàn tiền đầy đủ");
            }
            // Đổi hàng: chờ admin chọn variant mới trong form exchange
            return request;
        } else if (condition == ReturnCondition.THIEU_PHU_KIEN) {
            if (isRefund) {
                BigDecimal partialRefund = request.getSoTienHoan().multiply(refundPolicyService.getMaxRefundRateForDamaged());
                request.setSoTienThucTeHoan(partialRefund);
                refundRequestRepository.save(request);
            }
        } else if (condition == ReturnCondition.VO_VANG) {
            request.setTrangThai("TU_CHOI");
            request.setNgayXuLy(LocalDateTime.now());
            request.setGhiChuXuLy("Hàng bị vỡ do khách đóng gói không cẩn thận: " + ghiChu);
            refundRequestRepository.save(request);

            orderRepository.findById(request.getOrderId()).ifPresent(order -> {
                order.setTrangThaiDon("TU_CHOI_HOAN_TIEN");
                orderRepository.save(order);
            });
        }

        return request;
    }

    public RefundRequest completeRefund(Integer id, Integer adminId, String ghiChu) {
        RefundRequest request = getById(id);
        if (!"DA_DUYET".equals(request.getTrangThai()) && !"DA_NHAN_HANG_TRA".equals(request.getTrangThai())) {
            throw new RuntimeException("Yêu cầu không ở trạng thái có thể hoàn tiền");
        }

        request.setTrangThai("DA_HOAN_TIEN");
        request.setNguoiXuLyId(adminId);
        request.setNgayXuLy(LocalDateTime.now());
        request.setGhiChuXuLy(ghiChu);

        BigDecimal actualRefund = request.getSoTienThucTeHoan() != null ? request.getSoTienThucTeHoan() : request.getSoTienHoan();
        request.setSoTienThucTeHoan(actualRefund);
        
        // Nếu thanh toán VNPAY, gọi API refund VNPAY
        Order order = orderRepository.findById(request.getOrderId()).orElse(null);
        if (order != null && order.getPhuongThucTT() != null && "VNPAY".equals(order.getPhuongThucTT()) && vnpayService.isConfigured()) {
            try {
                // Lấy thông tin giao dịch VNPAY từ order
                String txnRef = "DUASTORE" + order.getId(); // txnRef format used when creating payment
                long amount = actualRefund.multiply(BigDecimal.valueOf(100)).longValue(); // VNPAY amount in cents
                String transactionNo = order.getVnpTransactionNo(); // VNPAY transaction number
                String transactionDate = order.getNgayDat() != null ? 
                    new java.text.SimpleDateFormat("yyyyMMddHHmmss").format(java.util.Date.from(order.getNgayDat().atZone(java.time.ZoneId.systemDefault()).toInstant())) 
                    : new java.text.SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
                
                Map<String, String> refundResult = vnpayService.refundTransaction(
                        txnRef,
                        amount,
                        transactionNo != null ? transactionNo : "0",
                        transactionDate,
                        "admin",
                        "Hoan tien don hang #" + order.getId(),
                        "127.0.0.1"
                );
                
                if ("true".equals(refundResult.get("success")) && "00".equals(refundResult.get("vnp_ResponseCode"))) {
                    request.setPhuongThucHoanTien("VNPAY_REFUND");
                    request.setMaVanDonTra(refundResult.get("vnp_TransactionNo"));
                    log.info("VNPAY refund successful for order {}: {}", order.getMaDon(), refundResult);
                } else {
                    log.warn("VNPAY refund failed for order {}: {}", order.getMaDon(), refundResult);
                    request.setPhuongThucHoanTien("VNPAY_REFUND_FAILED");
                    request.setGhiChuXuLy(ghiChu + " | VNPAY refund failed: " + refundResult.get("message"));
                }
            } catch (Exception e) {
                log.error("Error calling VNPAY refund for order {}: {}", request.getOrderId(), e.getMessage(), e);
                request.setPhuongThucHoanTien("VNPAY_REFUND_ERROR");
                request.setGhiChuXuLy(ghiChu + " | VNPAY refund error: " + e.getMessage());
            }
        } else {
            request.setPhuongThucHoanTien("CHUYEN_KHOAN");
        }
        
        refundRequestRepository.save(request);

        orderRepository.findById(request.getOrderId()).ifPresent(o -> {
            String oldStatus = o.getTrangThaiDon();
            o.setTrangThaiDon("DA_HOAN_TIEN");
            orderRepository.save(o);
            User admin = userRepository.findById(adminId).orElse(null);
            orderStatusLogService.ghiLog(o, OrderEventType.REFUND_ORDER, admin,
                    oldStatus, "DA_HOAN_TIEN", "Hoàn tiền: " + actualRefund + " VNĐ. " + ghiChu);
        });

        deductLoyaltyPointsForRefund(request);

        // Send email notification - refund completed
        sendRefundCompletedEmail(request);

        return request;
    }

    public RefundRequest processExchange(Integer id, Integer newVariantId, Integer adminId, String ghiChu) {
        RefundRequest request = getById(id);
        if (!"DA_DUYET".equals(request.getTrangThai()) && !"DA_NHAN_HANG_TRA".equals(request.getTrangThai())) {
            throw new RuntimeException("Yêu cầu không ở trạng thái có thể đổi hàng");
        }

        ProductVariant newVariant = productVariantRepository.findById(newVariantId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể mới"));
        
        // Check new variant is active and has stock
        if (!Boolean.TRUE.equals(newVariant.isActive())) {
            throw new RuntimeException("Biến thể mới không còn hoạt động");
        }
        if (newVariant.getSoLuongTon() == null || newVariant.getSoLuongTon() <= 0) {
            throw new RuntimeException("Biến thể mới đã hết hàng");
        }

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        OrderItem oldItem = order.getOrderItems().stream()
                .filter(i -> i.getVariantId() != null)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Không tìm thấy item để đổi"));

        BigDecimal priceDiff = refundPolicyService.calculateExchangePriceDiff(oldItem, newVariant);

        RefundReason reason = RefundReason.fromCode(request.getLyDoChiTiet());
        if (reason.isShopFault()) {
            request.setPhiShipTraLai(order.getPhiVanChuyen());
        } else {
            request.setPhiShipTraLai(BigDecimal.ZERO);
        }

        // Restore stock for old variant
        if (oldItem.getVariantId() != null) {
            ProductVariant oldVariant = productVariantRepository.findById(oldItem.getVariantId()).orElse(null);
            if (oldVariant != null) {
                oldVariant.setSoLuongTon(oldVariant.getSoLuongTon() + oldItem.getSoLuong());
                productVariantRepository.save(oldVariant);
            }
        }

        request.setVariantMoiId(newVariantId);
        request.setTrangThai("DA_HOAN_TIEN");
        request.setNguoiXuLyId(adminId);
        request.setNgayXuLy(LocalDateTime.now());
        request.setGhiChuXuLy("Đổi sang: " + newVariant.getTenBienThe() + ". Chênh lệch: " + priceDiff + ". " + ghiChu);
        request.setSoTienThucTeHoan(priceDiff.compareTo(BigDecimal.ZERO) > 0 ? priceDiff : BigDecimal.ZERO);
        refundRequestRepository.save(request);

        // Deduct stock for new variant
        newVariant.setSoLuongTon(newVariant.getSoLuongTon() - oldItem.getSoLuong());
        productVariantRepository.save(newVariant);

        oldItem.setVariantId(newVariant.getId());
        BigDecimal newPrice = newVariant.getGiaKhuyenMai() != null ? newVariant.getGiaKhuyenMai() : newVariant.getGiaGoc();
        oldItem.setDonGia(newPrice);
        oldItem.setThanhTien(newPrice.multiply(BigDecimal.valueOf(oldItem.getSoLuong())));
        oldItem.setTenBienThe(newVariant.getTenBienThe());
        if (newVariant.getProduct() != null) {
            oldItem.setTenSanPham(newVariant.getProduct().getTenSanPham());
        }
        oldItem.setHinhAnhSP(newVariant.getHinhAnh());

        order.setTrangThaiDon("DA_HOAN_TIEN");
        orderRepository.save(order);

        User admin = userRepository.findById(adminId).orElse(null);
        orderStatusLogService.ghiLog(order, OrderEventType.STATUS_CHANGE, admin,
                "DA_NHAN_HANG_TRA", "DA_HOAN_TIEN", "Đổi hàng: " + ghiChu);

        deductLoyaltyPointsForExchange(request.getUserId(), request.getOrderId(), "Đổi hàng cho đơn #" + request.getOrderId());

        // Send email notification - exchange completed
        sendRefundCompletedEmail(request);

        return request;
    }

    public RefundRequest updateReturnTracking(Integer id, String maVanDonTra, Integer adminId) {
        RefundRequest request = getById(id);
        request.setMaVanDonTra(maVanDonTra);
        refundRequestRepository.save(request);
        return request;
    }

    public RefundRequest saveActualPhoto(Integer id, String url) {
        RefundRequest request = getById(id);
        request.setAnhThucTe(url);
        return refundRequestRepository.save(request);
    }

    private void deductLoyaltyPointsForRefund(RefundRequest request) {
        orderRepository.findById(request.getOrderId()).ifPresent(order -> {
            if ("DA_HOAN_THANH".equals(order.getTrangThaiDon()) && order.getUser() != null) {
                int rate = loyaltyPointsService.getPointsEarnRate();
                int points = order.getTongThanhToan().divideToIntegralValue(BigDecimal.valueOf(rate)).intValue();
                if (points > 0) {
                    loyaltyPointsService.adjustPoints(order.getUser().getId(), -points,
                            "Hoàn điểm từ đơn #" + order.getId() + " (hoàn tiền)", "Hệ thống");
                }
            }
        });
    }

    private void deductLoyaltyPointsForExchange(Integer userId, Integer orderId, String reason) {
        orderRepository.findById(orderId).ifPresent(order -> {
            if (order.getUser() != null) {
                int rate = loyaltyPointsService.getPointsEarnRate();
                int points = order.getTongThanhToan().divideToIntegralValue(BigDecimal.valueOf(rate)).intValue();
                if (points > 0) {
                    loyaltyPointsService.adjustPoints(userId, -points,
                            reason, "Hệ thống");
                }
            }
        });
    }

    @Transactional(readOnly = true)
    public Map<Integer, String> getCustomerNames(List<RefundRequest> refunds) {
        Map<Integer, String> names = new java.util.HashMap<>();
        if (refunds == null || refunds.isEmpty()) {
            return names;
        }
        List<Integer> userIds = refunds.stream()
                .map(RefundRequest::getUserId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        userRepository.findAllById(userIds).forEach(u -> names.put(u.getId(), u.getHoTen()));
        return names;
    }

    @Transactional(readOnly = true)
    public long getPendingCount() {
        return refundRequestRepository.countByTrangThai("CHO_DUYET");
    }

    @Transactional(readOnly = true)
    public long getCompletedCount(LocalDateTime from, LocalDateTime to) {
        return refundRequestRepository.countByTrangThaiAndNgayYeuCauBetween("DA_HOAN_TIEN", from, to);
    }

    @Transactional(readOnly = true)
    public List<RefundRequest> getByUser(Integer userId) {
        return refundRequestRepository.findByUserIdOrderByNgayYeuCauDesc(userId);
    }

    @Transactional(readOnly = true)
    public List<RefundRequest> getByUserAndStatus(Integer userId, String trangThai) {
        return refundRequestRepository.findByUserIdAndTrangThaiOrderByNgayYeuCauDesc(userId, trangThai);
    }

    @Transactional(readOnly = true)
    public Page<RefundRequest> getPagedByStatus(String trangThai, Pageable pageable) {
        return refundRequestRepository.findByTrangThaiOrderByNgayYeuCauDesc(trangThai, pageable);
    }

    @Transactional(readOnly = true)
    public long getCountByStatus(String trangThai) {
        return refundRequestRepository.countByTrangThai(trangThai);
    }

    @Transactional(readOnly = true)
    public List<ProductVariant> getAvailableVariantsForExchange(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        List<Integer> productIds = order.getOrderItems().stream()
                .map(item -> item.getProductId())
                .distinct()
                .toList();
        return productVariantRepository.findByProductIdInAndIsActiveTrue(productIds);
    }

    @Transactional(readOnly = true)
    public BigDecimal getOldItemPriceForExchange(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        return order.getOrderItems().stream()
                .filter(i -> i.getVariantId() != null)
                .findFirst()
                .map(OrderItem::getDonGia)
                .orElse(BigDecimal.ZERO);
    }

    // ==================== EMAIL NOTIFICATIONS ====================

    public void sendRefundApprovedEmail(RefundRequest refund) {
        try {
            Order order = orderRepository.findById(refund.getOrderId()).orElse(null);
            if (order == null || order.getUser() == null) return;

            String warehouseAddress = siteSettingService.getValue("refund_warehouse_address",
                    "Kho DuaStore - 123 Tran Hung Dao, May To, Ngo Quyen, Hai Phong");
            String contactPhone = siteSettingService.getValue("refund_contact_phone", "0225.123.4567");
            String contactEmail = siteSettingService.getValue("refund_contact_email", "support@duastore.vn");

            String subject = "[DuaStore] Yêu cầu " + getRefundTypeDisplay(refund.getLoaiYeuCau()) + " đã được duyệt - Đơn #" + order.getMaDon();

            String html = buildRefundApprovedEmailHtml(order, refund, warehouseAddress, contactPhone, contactEmail);
            String plainText = buildRefundApprovedEmailPlain(order, refund, warehouseAddress, contactPhone, contactEmail);

            emailService.send(order.getUser().getEmail(), subject, html);
            log.info("Sent refund approved email for refund {}", refund.getId());
        } catch (Exception e) {
            log.warn("Failed to send refund approved email for refund {}: {}", refund.getId(), e.getMessage());
        }
    }

    public void sendWarehouseReceivedEmail(RefundRequest refund) {
        try {
            Order order = orderRepository.findById(refund.getOrderId()).orElse(null);
            if (order == null || order.getUser() == null) return;

            String subject = "[DuaStore] Kho đã nhận hàng trả - Đơn #" + order.getMaDon();

            String html = buildWarehouseReceivedEmailHtml(order, refund);
            String plainText = buildWarehouseReceivedEmailPlain(order, refund);

            emailService.send(order.getUser().getEmail(), subject, html);
            log.info("Sent warehouse received email for refund {}", refund.getId());
        } catch (Exception e) {
            log.warn("Failed to send warehouse received email for refund {}: {}", refund.getId(), e.getMessage());
        }
    }

    public void sendRefundCompletedEmail(RefundRequest refund) {
        try {
            Order order = orderRepository.findById(refund.getOrderId()).orElse(null);
            if (order == null || order.getUser() == null) return;

            String subject = "[DuaStore] " + getRefundTypeDisplay(refund.getLoaiYeuCau()) + " hoàn tất - Đơn #" + order.getMaDon();

            String html = buildRefundCompletedEmailHtml(order, refund);
            String plainText = buildRefundCompletedEmailPlain(order, refund);

            emailService.send(order.getUser().getEmail(), subject, html);
            log.info("Sent refund completed email for refund {}", refund.getId());
        } catch (Exception e) {
            log.warn("Failed to send refund completed email for refund {}: {}", refund.getId(), e.getMessage());
        }
    }

    private String getRefundTypeDisplay(String loaiYeuCau) {
        return switch (loaiYeuCau) {
            case "DOI_SIZE" -> "đổi size";
            case "DOI_MAU" -> "đổi màu";
            case "DOI_SAN_PHAM_KHAC" -> "đổi sản phẩm";
            default -> "hoàn tiền";
        };
    }

    private String resolveVariantDisplay(Integer variantId) {
        if (variantId == null) {
            return "—";
        }
        try {
            return productVariantRepository.findById(variantId)
                    .map(v -> (v.getProduct() != null ? v.getProduct().getTenSanPham() + " - " : "")
                            + v.getTenBienThe())
                    .orElse("Variant #" + variantId);
        } catch (Exception e) {
            return "Variant #" + variantId;
        }
    }

    private String buildRefundApprovedEmailHtml(Order order, RefundRequest refund, String warehouseAddress, String phone, String email) {
        String typeDisplay = getRefundTypeDisplay(refund.getLoaiYeuCau());
        String reasonDisplay = switch (refund.getLyDoChiTiet()) {
            case "LOI_HANG" -> "Lỗi hàng (vỡ, nứt, khác mô tả)";
            case "KHONG_DUNG_MO_TA" -> "Không đúng mô tả/hình ảnh";
            case "DOI_Y" -> "Đổi ý (không thích, mua nhầm)";
            default -> "Lý do khác";
        };

        return """
            <html><body style="font-family:Arial;background:#f5f5f5;padding:40px 0;">
              <table width="480" align="center"
                     style="background:#fff;border-radius:16px;overflow:hidden;
                            box-shadow:0 4px 24px rgba(0,0,0,.1);">
                <tr>
                  <td style="background:linear-gradient(135deg,#10b981,#059669);
                             padding:32px;text-align:center;">
                    <div style="color:#fff;font-size:24px;font-weight:800;">DuaStore</div>
                    <div style="color:rgba(255,255,255,.8);font-size:13px;">Đồ Thủy Tinh Cao Cấp</div>
                  </td>
                </tr>
                <tr>
                  <td style="padding:36px 40px;">
                    <p style="font-size:15px;color:#616161;">
                      Xin chào <strong>%s</strong>,
                    </p>
                    <p style="font-size:15px;color:#616161;">
                      Yêu cầu <strong>%s</strong> cho đơn hàng <strong>%s</strong> đã được <span style="color:#10b981;font-weight:bold;">DUYỆT</span>.
                    </p>
                    <div style="background:#f0fdf4;border:1px solid #86efac;border-radius:12px;padding:20px;margin:20px 0;">
                      <h4 style="margin:0 0 12px 0;color:#166534;font-size:15px;">
                        <i class="bi bi-truck"></i> Hướng dẫn gửi hàng trả về kho
                      </h4>
                      <ul style="margin:0;padding-left:20px;color:#166534;font-size:14px;line-height:1.8;">
                        <li>Đóng gói <strong>cẩn thận</strong>: đủ hộp gốc, phụ kiện, tem mác, không bị nguội/nứt thêm</li>
                        <li>In hoặc viết rõ <strong>Mã yêu cầu: %d</strong> và <strong>Mã đơn: %s</strong> trên bao bì</li>
                        <li>Gửi về địa chỉ kho:<br><strong>%s</strong></li>
                        <li>Sử dụng dịch vụ vận chuyển có theo dõi (GHN, Viettel Post, J&T...)</li>
                        <li>Sau khi gửi, <strong>cập nhật mã vận đơn</strong> tại trang theo dõi đơn hàng</li>
                      </ul>
                    </div>
                    <div style="background:#fef3c7;border:1px solid #fcd34d;border-radius:12px;padding:16px;margin:16px 0;font-size:13px;color:#92400e;">
                      <strong>Lưu ý quan trọng:</strong>
                      <ul style="margin:8px 0 0 0;padding-left:20px;line-height:1.7;">
                        <li>Lý do: %s</li>
                        <li>Loại yêu cầu: %s</li>
                        <li>Số tiền dự kiến: %sđ</li>
                        <li>%s</li>
                      </ul>
                    </div>
                    <p style="font-size:13px;color:#9e9e9e;margin-top:24px;">
                      Nếu có thắc mắc, liên hệ: <strong>%s</strong> | <strong>%s</strong>
                    </p>
                  </td>
                </tr>
                <tr>
                  <td style="background:#f9f9f9;padding:16px;text-align:center;border-top:1px solid #eee;">
                    <span style="font-size:12px;color:#bdbdbd;">&copy; 2025 DuaStore</span>
                  </td>
                </tr>
              </table>
            </body></html>
            """.formatted(
                order.getUser().getHoTen(),
                typeDisplay,
                order.getMaDon(),
                refund.getId(),
                order.getMaDon(),
                warehouseAddress,
                reasonDisplay,
                typeDisplay,
                java.text.NumberFormat.getInstance(java.util.Locale.US).format(refund.getSoTienHoan()),
                refund.getPhiShipTraLai() != null && refund.getPhiShipTraLai().compareTo(BigDecimal.ZERO) > 0
                        ? "(Shop chịu phí ship trả: " + java.text.NumberFormat.getInstance(java.util.Locale.US).format(refund.getPhiShipTraLai()) + "đ)"
                        : "(Khách chịu phí ship trả)",
                phone, email
            );
    }

    private String buildRefundApprovedEmailPlain(Order order, RefundRequest refund, String warehouseAddress, String phone, String email) {
        String typeDisplay = getRefundTypeDisplay(refund.getLoaiYeuCau());
        return "DuaStore - Yeu cau " + typeDisplay + " da duoc DUYET - Don #" + order.getMaDon() + "\n\n"
                + "Xin chao " + order.getUser().getHoTen() + ",\n\n"
                + "Yeu cau " + typeDisplay + " cho don hang " + order.getMaDon() + " da duoc duyet.\n\n"
                + "Huong dan gui hang tra ve kho:\n"
                + "1. Dong goi can than: du hop goc, phu kien, tem mac, khong bi hu them\n"
                + "2. Ghi ro Ma yeu cau: " + refund.getId() + " va Ma don: " + order.getMaDon() + " tren bao bi\n"
                + "3. Gui ve dia chi: " + warehouseAddress + "\n"
                + "4. Su dung dich vu van chuyen co theo doi (GHN, Viettel Post...)\n"
                + "5. Cap nhat ma van don tai trang theo doi don hang\n\n"
                + "Ly do: " + refund.getLyDoChiTiet() + "\n"
                + "Loai yeu cau: " + typeDisplay + "\n"
                + "So tien du kien: " + refund.getSoTienHoan() + "d\n"
                + (refund.getPhiShipTraLai() != null && refund.getPhiShipTraLai().compareTo(BigDecimal.ZERO) > 0
                        ? "(Shop chiu phi ship tra: " + refund.getPhiShipTraLai() + "d)\n"
                        : "(Khach chiu phi ship tra)\n")
                + "\nLien he: " + phone + " | " + email;
    }

    private String buildWarehouseReceivedEmailHtml(Order order, RefundRequest refund) {
        String typeDisplay = getRefundTypeDisplay(refund.getLoaiYeuCau());
        return """
            <html><body style="font-family:Arial;background:#f5f5f5;padding:40px 0;">
              <table width="480" align="center"
                     style="background:#fff;border-radius:16px;overflow:hidden;
                            box-shadow:0 4px 24px rgba(0,0,0,.1);">
                <tr>
                  <td style="background:linear-gradient(135deg,#3b82f6,#2563eb);
                             padding:32px;text-align:center;">
                    <div style="color:#fff;font-size:24px;font-weight:800;">DuaStore</div>
                    <div style="color:rgba(255,255,255,.8);font-size:13px;">Đồ Thủy Tinh Cao Cấp</div>
                  </td>
                </tr>
                <tr>
                  <td style="padding:36px 40px;">
                    <p style="font-size:15px;color:#616161;">
                      Xin chào <strong>%s</strong>,
                    </p>
                    <p style="font-size:15px;color:#616161;">
                      Kho đã nhận hàng trả cho đơn <strong>%s</strong> (Yêu cầu <strong>%s</strong> #%d).
                    </p>
                    <div style="background:#eff6ff;border:1px solid #93c5fd;border-radius:12px;padding:20px;margin:20px 0;">
                      <h4 style="margin:0 0 12px 0;color:#1e40af;font-size:15px;">
                        <i class="bi bi-clipboard-check"></i> Đang kiểm tra hàng
                      </h4>
                      <p style="margin:0;color:#1e40af;font-size:14px;line-height:1.6;">
                        Nhân viên kho đang kiểm tra tình trạng thực tế của hàng. Quá trình này thường mất <strong>1-2 ngày làm việc</strong>.
                        Chúng tôi sẽ thông báo kết quả qua email và trang theo dõi đơn hàng.
                      </p>
                    </div>
                    <p style="font-size:13px;color:#9e9e9e;margin-top:24px;">
                      Xem chi tiết: <a href="#" style="color:#3b82f6;">Trang theo dõi đơn hàng</a>
                    </p>
                  </td>
                </tr>
                <tr>
                  <td style="background:#f9f9f9;padding:16px;text-align:center;border-top:1px solid #eee;">
                    <span style="font-size:12px;color:#bdbdbd;">&copy; 2025 DuaStore</span>
                  </td>
                </tr>
              </table>
            </body></html>
            """.formatted(order.getUser().getHoTen(), order.getMaDon(), typeDisplay, refund.getId());
    }

    private String buildWarehouseReceivedEmailPlain(Order order, RefundRequest refund) {
        String typeDisplay = getRefundTypeDisplay(refund.getLoaiYeuCau());
        return "DuaStore - Kho da nhan hang tra - Don #" + order.getMaDon() + "\n\n"
                + "Xin chao " + order.getUser().getHoTen() + ",\n\n"
                + "Kho da nhan hang tra cho don " + order.getMaDon() + " (Yeu cau " + typeDisplay + " #" + refund.getId() + ").\n\n"
                + "Nhan vien kho dang kiem tra tinh trang that te cua hang. Qua trinh nay thuong mat 1-2 ngay lam viec.\n"
                + "Chung toi se thong bao ket qua qua email va trang theo doi don hang.\n\n"
                + "Xem chi tiet: Trang theo doi don hang";
    }

    private String buildRefundCompletedEmailHtml(Order order, RefundRequest refund) {
        String typeDisplay = getRefundTypeDisplay(refund.getLoaiYeuCau());
        String amountDisplay = java.text.NumberFormat.getInstance(java.util.Locale.US).format(
                refund.getSoTienThucTeHoan() != null ? refund.getSoTienThucTeHoan() : refund.getSoTienHoan());

        boolean isExchange = refund.getLoaiYeuCau() != null && !refund.getLoaiYeuCau().equals("HOAN_TIEN");
        String actionText = isExchange ? "Đổi hàng thành công" : "Hoàn tiền thành công";
        String detailText;
        if (isExchange) {
            String variantName = resolveVariantDisplay(refund.getVariantMoiId());
            String diffNote = refund.getSoTienThucTeHoan() != null && refund.getSoTienThucTeHoan().compareTo(BigDecimal.ZERO) > 0
                    ? " Khách thanh toán thêm: " + amountDisplay + "đ"
                    : "";
            detailText = "Đơn hàng đã được đổi sang: " + variantName + "." + diffNote;
        } else {
            detailText = "Số tiền " + amountDisplay + "đ đã được chuyển khoản về tài khoản của bạn. Thời gian đến tài khoản: 1-3 ngày làm việc.";
        }

        return """
            <html><body style="font-family:Arial;background:#f5f5f5;padding:40px 0;">
              <table width="480" align="center"
                     style="background:#fff;border-radius:16px;overflow:hidden;
                            box-shadow:0 4px 24px rgba(0,0,0,.1);">
                <tr>
                  <td style="background:linear-gradient(135deg,#10b981,#059669);
                             padding:32px;text-align:center;">
                    <div style="color:#fff;font-size:24px;font-weight:800;">DuaStore</div>
                    <div style="color:rgba(255,255,255,.8);font-size:13px;">Đồ Thủy Tinh Cao Cấp</div>
                  </td>
                </tr>
                <tr>
                  <td style="padding:36px 40px;">
                    <p style="font-size:15px;color:#616161;">
                      Xin chào <strong>%s</strong>,
                    </p>
                    <div style="background:#f0fdf4;border:2px solid #86efac;border-radius:12px;padding:24px;text-align:center;margin-bottom:24px;">
                      <div style="font-size:28px;font-weight:900;color:#10b981;">✓ %s</div>
                      <div style="font-size:14px;color:#166534;margin-top:8px;">Đơn hàng <strong>%s</strong></div>
                    </div>
                    <p style="font-size:15px;color:#616161;">%s</p>
                    <div style="background:#f9fafb;border:1px solid #e5e7eb;border-radius:12px;padding:20px;margin:20px 0;">
                      <table style="width:100%;font-size:14px;color:#374151;">
                        <tr><td style="padding:6px 0;color:#9ca3af;">Mã đơn</td><td style="padding:6px 0;text-align:right;font-weight:600;">%s</td></tr>
                        <tr><td style="padding:6px 0;color:#9ca3af;">Loại yêu cầu</td><td style="padding:6px 0;text-align:right;">%s</td></tr>
                        <tr><td style="padding:6px 0;color:#9ca3af;">Số tiền</td><td style="padding:6px 0;text-align:right;font-weight:600;color:#10b981;">%sđ</td></tr>
                        <tr><td style="padding:6px 0;color:#9ca3af;">Phương thức</td><td style="padding:6px 0;text-align:right;">Chuyển khoản ngân hàng</td></tr>
                      </table>
                    </div>
                    <p style="font-size:13px;color:#9e9e9e;margin-top:24px;">
                      Cảm ơn bạn đã tin tưởng mua sắm tại DuaStore!
                    </p>
                  </td>
                </tr>
                <tr>
                  <td style="background:#f9f9f9;padding:16px;text-align:center;border-top:1px solid #eee;">
                    <span style="font-size:12px;color:#bdbdbd;">&copy; 2025 DuaStore</span>
                  </td>
                </tr>
              </table>
            </body></html>
            """.formatted(order.getUser().getHoTen(), actionText, order.getMaDon(), detailText, order.getMaDon(), typeDisplay, amountDisplay);
    }

    private String buildRefundCompletedEmailPlain(Order order, RefundRequest refund) {
        String typeDisplay = getRefundTypeDisplay(refund.getLoaiYeuCau());
        String amountDisplay = java.text.NumberFormat.getInstance(java.util.Locale.US).format(
                refund.getSoTienThucTeHoan() != null ? refund.getSoTienThucTeHoan() : refund.getSoTienHoan());
        boolean isExchange = refund.getLoaiYeuCau() != null && !refund.getLoaiYeuCau().equals("HOAN_TIEN");
        String actionText = isExchange ? "Doi hang thanh cong" : "Hoan tien thanh cong";
        return "DuaStore - " + actionText + " - Don #" + order.getMaDon() + "\n\n"
                + "Xin chao " + order.getUser().getHoTen() + ",\n\n"
                + actionText + " cho don hang " + order.getMaDon() + ".\n\n"
                + "Chi tiet:\n"
                + "  Ma don: " + order.getMaDon() + "\n"
                + "  Loai yeu cau: " + typeDisplay + "\n"
                + "  So tien: " + amountDisplay + "d\n"
                + "  Phuong thuc: Chuyen khoan ngan hang\n\n"
                + "Cam on ban da tin tuong mua sam tai DuaStore!";
    }
}