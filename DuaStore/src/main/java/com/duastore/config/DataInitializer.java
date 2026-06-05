package com.duastore.config;

import com.duastore.model.Promotion;
import com.duastore.repository.PromotionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final PromotionRepository promotionRepository;

    public DataInitializer(PromotionRepository promotionRepository) {
        this.promotionRepository = promotionRepository;
    }

    @Override
    public void run(String... args) {
        List<Promotion> existing = promotionRepository.findAll();
        if (!existing.isEmpty()) {
            for (Promotion p : existing) {
                p.setDenNgay(LocalDateTime.now().plusMonths(6));
                p.setTuNgay(LocalDateTime.now().minusDays(1));
                if (p.getDaDung() == null) p.setDaDung(0);
                if (p.getIsActive() == null) p.setIsActive(true);
                promotionRepository.save(p);
            }
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        Promotion p1 = new Promotion();
        p1.setMaCode("KHAIHANG");
        p1.setTenChuongTrinh("Khai trương DuaStore");
        p1.setLoaiGiam("PHAN_TRAM");
        p1.setGiaTriGiam(new BigDecimal("15"));
        p1.setDonHangToiThieu(new BigDecimal("200000"));
        p1.setGiamToiDa(new BigDecimal("100000"));
        p1.setSoLanDung(200);
        p1.setDaDung(0);
        p1.setTuNgay(now.minusDays(1));
        p1.setDenNgay(now.plusMonths(6));
        p1.setIsActive(true);

        Promotion p2 = new Promotion();
        p2.setMaCode("FREESHIP");
        p2.setTenChuongTrinh("Miễn phí vận chuyển");
        p2.setLoaiGiam("SO_TIEN");
        p2.setGiaTriGiam(new BigDecimal("30000"));
        p2.setDonHangToiThieu(new BigDecimal("500000"));
        p2.setDaDung(0);
        p2.setTuNgay(now.minusDays(1));
        p2.setDenNgay(now.plusMonths(6));
        p2.setIsActive(true);

        Promotion p3 = new Promotion();
        p3.setMaCode("DECO50K");
        p3.setTenChuongTrinh("Giảm 50k đơn từ 300k");
        p3.setLoaiGiam("SO_TIEN");
        p3.setGiaTriGiam(new BigDecimal("50000"));
        p3.setDonHangToiThieu(new BigDecimal("300000"));
        p3.setSoLanDung(100);
        p3.setDaDung(0);
        p3.setTuNgay(now.minusDays(1));
        p3.setDenNgay(now.plusMonths(6));
        p3.setIsActive(true);

        promotionRepository.saveAll(List.of(p1, p2, p3));
    }
}
