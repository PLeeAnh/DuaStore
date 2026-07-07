package com.duastore.service;

import com.duastore.model.Order;
import com.duastore.model.Role;
import com.duastore.model.User;
import com.duastore.repository.OrderRepository;
import com.duastore.repository.RoleRepository;
import com.duastore.repository.UserRepository;
import com.duastore.service.admin.AdminDashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AdminDashboardServiceTest {

    @Autowired
    private AdminDashboardService dashboardService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private User user;

    @BeforeEach
    void setUp() {
        Role role = roleRepository.findByName("USER");
        if (role == null) {
            role = new Role();
            role.setName("USER");
            role = roleRepository.save(role);
        }

        user = new User();
        user.setUsername("dashboard_user");
        user.setEmail("dashboard_user@test.com");
        user.setHoTen("Dashboard User");
        user.setPassword("pass");
        user.setIsActive(true);
        user.setRoles(Set.of(role));
        user = userRepository.save(user);
    }

    private Order createOrder(String maDon, String trangThaiDon, String phuongThucTT,
                               BigDecimal tongThanhToan, LocalDateTime ngayDat) {
        Order o = new Order();
        o.setMaDon(maDon);
        o.setUser(user);
        o.setSnapTenNguoiNhan("Test");
        o.setSnapSoDienThoai("0900000000");
        o.setSnapDiaChi("123 Test St");
        o.setTienHang(tongThanhToan);
        o.setPhiVanChuyen(BigDecimal.ZERO);
        o.setTienGiam(BigDecimal.ZERO);
        o.setTongThanhToan(tongThanhToan);
        o.setPhuongThucTT(phuongThucTT);
        o.setPhuongThucGiaoHang("SHIP");
        o.setTrangThaiTT("DA_THANH_TOAN");
        o.setTrangThaiDon(trangThaiDon);
        o.setNgayDat(ngayDat);
        return orderRepository.save(o);
    }

    // ------ getPaymentMethodDistribution ------

    @Test
    void getPaymentMethodDistribution_returnsDistinctMethods() {
        createOrder("PM-001", "DA_HOAN_THANH", "COD", new BigDecimal("100000"), LocalDateTime.now().minusDays(1));
        createOrder("PM-002", "DA_HOAN_THANH", "VNPAY", new BigDecimal("200000"), LocalDateTime.now().minusDays(2));
        createOrder("PM-003", "DA_HOAN_THANH", "COD", new BigDecimal("150000"), LocalDateTime.now().minusDays(3));

        Map<String, Long> dist = dashboardService.getPaymentMethodDistribution();

        assertThat(dist).containsKey("COD");
        assertThat(dist).containsKey("VNPAY");
        assertThat(dist.get("COD")).isEqualTo(2);
        assertThat(dist.get("VNPAY")).isEqualTo(1);
    }

    @Test
    void getPaymentMethodDistribution_noOrders_returnsEmpty() {
        Map<String, Long> dist = dashboardService.getPaymentMethodDistribution();
        assertThat(dist).isEmpty();
    }

    // ------ getSalesFunnel ------

    @Test
    void getSalesFunnel_returnsAllStages() {
        createOrder("SF-001", "CHO_XAC_NHAN", "COD", new BigDecimal("100000"), LocalDateTime.now().minusDays(1));
        createOrder("SF-002", "DA_XAC_NHAN", "COD", new BigDecimal("200000"), LocalDateTime.now().minusDays(2));
        createOrder("SF-003", "DANG_GIAO", "VNPAY", new BigDecimal("300000"), LocalDateTime.now().minusDays(3));
        createOrder("SF-004", "DA_GIAO", "VNPAY", new BigDecimal("400000"), LocalDateTime.now().minusDays(4));
        createOrder("SF-005", "DA_HOAN_THANH", "COD", new BigDecimal("500000"), LocalDateTime.now().minusDays(5));

        List<Map<String, Object>> funnel = dashboardService.getSalesFunnel();

        assertThat(funnel).hasSize(5);
        assertThat(funnel.get(0).get("status")).isEqualTo("CHO_XAC_NHAN");
        assertThat(funnel.get(4).get("status")).isEqualTo("DA_HOAN_THANH");
        assertThat(funnel.get(0).get("isFirst")).isEqualTo(true);
    }

    // ------ getStatComparison ------

    @Test
    void getStatComparison_returnsAllFields() {
        createOrder("SC-001", "DA_HOAN_THANH", "COD", new BigDecimal("300000"), LocalDateTime.now().minusHours(1));
        createOrder("SC-002", "CHO_XAC_NHAN", "VNPAY", new BigDecimal("500000"), LocalDateTime.now().minusDays(15));

        Map<String, Object> stats = dashboardService.getStatComparison();

        assertThat(stats).containsKeys("todayOrders", "todayOrdersChange",
                "ordersThisMonth", "ordersChange",
                "revenueThisMonth", "revenueChange",
                "newCustomersThisMonth", "customersChange");
    }

    // ------ getRevenueGrowth ------

    @Test
    void getRevenueGrowth_withRevenue_returnsFormattedString() {
        createOrder("RG-001", "DA_HOAN_THANH", "VNPAY", new BigDecimal("1000000"), LocalDateTime.now().minusHours(1));

        String growth = dashboardService.getRevenueGrowth();

        assertThat(growth).endsWith("%");
    }

    @Test
    void getRevenueGrowth_noRevenue_returnsZero() {
        String growth = dashboardService.getRevenueGrowth();
        assertThat(growth).isEqualTo("0%");
    }
}
