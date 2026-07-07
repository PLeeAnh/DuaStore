package com.duastore.service;

import com.duastore.model.Promotion;
import com.duastore.model.VoucherType;
import com.duastore.repository.PromotionRepository;
import com.duastore.repository.UserRepository;
import com.duastore.service.client.VoucherWalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class VoucherWalletServiceTest {

    @Autowired
    private VoucherWalletService voucherWalletService;

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private UserRepository userRepository;

    private Promotion activePromo;

    @BeforeEach
    void setUp() {
        activePromo = new Promotion();
        activePromo.setMaCode("TEST_SAVE");
        activePromo.setTenChuongTrinh("Test Voucher");
        activePromo.setLoaiGiam("PHAN_TRAM");
        activePromo.setGiaTriGiam(java.math.BigDecimal.TEN);
        activePromo.setIsActive(true);
        activePromo.setVoucherType(VoucherType.VOUCHER);
        activePromo.setMaxClaimsPerUser(3);
        activePromo.setSavedCount(0);
        activePromo.setDaDung(0);
        activePromo.setTuNgay(LocalDateTime.now().minusDays(1));
        activePromo.setDenNgay(LocalDateTime.now().plusDays(30));
        activePromo = promotionRepository.save(activePromo);
    }

    @Test
    void saveVoucher_withinMaxClaims_succeeds() {
        var voucher = voucherWalletService.saveVoucher(999, activePromo.getId());
        assertThat(voucher).isNotNull();
        assertThat(voucher.getUserId()).isEqualTo(999);
    }

    @Test
    void saveVoucher_exceedsMaxClaims_throws() {
        activePromo.setMaxClaims(2);
        activePromo.setSavedCount(2);
        promotionRepository.save(activePromo);

        assertThatThrownBy(() -> voucherWalletService.saveVoucher(999, activePromo.getId()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("hết lượt lưu");
    }
}
