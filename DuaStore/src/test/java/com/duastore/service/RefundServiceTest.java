package com.duastore.service;

import com.duastore.model.RefundRequest;
import com.duastore.model.Role;
import com.duastore.model.User;
import com.duastore.repository.RefundRequestRepository;
import com.duastore.repository.RoleRepository;
import com.duastore.repository.UserRepository;
import com.duastore.service.admin.RefundService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RefundServiceTest {

    @Autowired
    private RefundService refundService;

    @Autowired
    private RefundRequestRepository refundRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User user;
    private User admin;

    @BeforeEach
    void setUp() {
        Role userRole = roleRepository.findByName("USER");
        if (userRole == null) {
            userRole = new Role();
            userRole.setName("USER");
            roleRepository.save(userRole);
        }

        Role adminRole = roleRepository.findByName("ADMIN");
        if (adminRole == null) {
            adminRole = new Role();
            adminRole.setName("ADMIN");
            roleRepository.save(adminRole);
        }

        user = new User();
        user.setUsername("refund_user");
        user.setEmail("refund_user@test.com");
        user.setHoTen("Refund User");
        user.setPassword(passwordEncoder.encode("user"));
        user.setIsActive(true);
        user.setRoles(Set.of(userRole));
        user = userRepository.save(user);

        admin = new User();
        admin.setUsername("refund_admin");
        admin.setEmail("refund_admin@test.com");
        admin.setHoTen("Refund Admin");
        admin.setPassword(passwordEncoder.encode("admin"));
        admin.setIsActive(true);
        admin.setRoles(Set.of(adminRole));
        admin = userRepository.save(admin);
    }

    @Test
    void getAll_returnsAllRequestsDescending() {
        refundService.create(createRequest(user.getId()));
        refundService.create(createRequest(user.getId()));

        List<RefundRequest> list = refundService.getAll();

        assertThat(list).hasSize(2);
    }

    @Test
    void create_savesRefundRequest() {
        RefundRequest request = createRequest(user.getId());

        RefundRequest saved = refundService.create(request);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUserId()).isEqualTo(user.getId());
        assertThat(saved.getSoTienHoan()).isEqualByComparingTo(new BigDecimal("500000"));
        assertThat(saved.getTrangThai()).isEqualTo("CHO_DUYET");
        assertThat(saved.getNgayYeuCau()).isNotNull();
    }

    @Test
    void getById_existingId_returnsRequest() {
        RefundRequest saved = refundService.create(createRequest(user.getId()));

        RefundRequest found = refundService.getById(saved.getId());

        assertThat(found.getId()).isEqualTo(saved.getId());
    }

    @Test
    void getById_nonExistingId_throwsException() {
        assertThatThrownBy(() -> refundService.getById(99999))
                .hasMessageContaining("Không tìm thấy yêu cầu hoàn tiền");
    }

    @Test
    void approve_setsStatusAndAdminId() {
        RefundRequest saved = refundService.create(createRequest(user.getId()));

        RefundRequest approved = refundService.approve(saved.getId(), admin.getId(), "OK");

        assertThat(approved.getTrangThai()).isEqualTo("DA_DUYET");
        assertThat(approved.getNguoiXuLyId()).isEqualTo(admin.getId());
        assertThat(approved.getGhiChuXuLy()).isEqualTo("OK");
        assertThat(approved.getNgayXuLy()).isNotNull();
    }

    @Test
    void reject_setsStatusAndAdminId() {
        RefundRequest saved = refundService.create(createRequest(user.getId()));

        RefundRequest rejected = refundService.reject(saved.getId(), admin.getId(), "Không hợp lệ");

        assertThat(rejected.getTrangThai()).isEqualTo("TU_CHOI");
        assertThat(rejected.getNguoiXuLyId()).isEqualTo(admin.getId());
        assertThat(rejected.getGhiChuXuLy()).isEqualTo("Không hợp lệ");
        assertThat(rejected.getNgayXuLy()).isNotNull();
    }

    @Test
    void getPendingCount_returnsCorrectCount() {
        refundService.create(createRequest(user.getId()));
        refundService.create(createRequest(user.getId()));

        assertThat(refundService.getPendingCount()).isEqualTo(2);

        RefundRequest created = refundService.create(createRequest(user.getId()));
        refundService.approve(created.getId(), admin.getId(), null);

        assertThat(refundService.getPendingCount()).isEqualTo(2);
    }

    @Test
    void getCompletedCount_returnsCorrectCount() {
        refundService.create(createRequest(user.getId()));

        RefundRequest req = refundService.create(createRequest(user.getId()));
        refundService.approve(req.getId(), admin.getId(), null);

        LocalDate today = LocalDate.now();
        assertThat(refundService.getCompletedCount(today.minusDays(1), today.plusDays(1))).isEqualTo(1);
    }

    @Test
    void getByUser_returnsUserRequests() {
        refundService.create(createRequest(user.getId()));
        refundService.create(createRequest(user.getId()));

        List<RefundRequest> userRequests = refundService.getByUser(user.getId());

        assertThat(userRequests).hasSize(2);
        assertThat(userRequests).allMatch(r -> r.getUserId().equals(user.getId()));
    }

    private RefundRequest createRequest(Integer userId) {
        RefundRequest request = new RefundRequest();
        request.setOrderId(1);
        request.setUserId(userId);
        request.setLydo("Sản phẩm lỗi");
        request.setSoTienHoan(new BigDecimal("500000"));
        request.setPhuongThucHoan("BANKING");
        return request;
    }
}
