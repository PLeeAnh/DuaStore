package com.duastore.service.admin;

import com.duastore.model.RefundRequest;
import com.duastore.repository.RefundRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@Transactional
public class RefundService {

    private final RefundRequestRepository refundRequestRepository;

    public RefundService(RefundRequestRepository refundRequestRepository) {
        this.refundRequestRepository = refundRequestRepository;
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

    public RefundRequest create(RefundRequest request) {
        return refundRequestRepository.save(request);
    }

    public RefundRequest approve(Integer id, Integer adminId, String ghiChu) {
        RefundRequest request = getById(id);
        request.setTrangThai("DA_DUYET");
        request.setNguoiXuLyId(adminId);
        request.setNgayXuLy(LocalDateTime.now());
        request.setGhiChuXuLy(ghiChu);
        return refundRequestRepository.save(request);
    }

    public RefundRequest reject(Integer id, Integer adminId, String ghiChu) {
        RefundRequest request = getById(id);
        request.setTrangThai("TU_CHOI");
        request.setNguoiXuLyId(adminId);
        request.setNgayXuLy(LocalDateTime.now());
        request.setGhiChuXuLy(ghiChu);
        return refundRequestRepository.save(request);
    }

    @Transactional(readOnly = true)
    public long getPendingCount() {
        return refundRequestRepository.countByTrangThai("CHO_DUYET");
    }

    @Transactional(readOnly = true)
    public long getCompletedCount(LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);
        return refundRequestRepository.countByTrangThaiAndNgayYeuCauBetween("DA_DUYET", start, end);
    }

    @Transactional(readOnly = true)
    public List<RefundRequest> getByUser(Integer userId) {
        return refundRequestRepository.findByUserIdOrderByNgayYeuCauDesc(userId);
    }
}
