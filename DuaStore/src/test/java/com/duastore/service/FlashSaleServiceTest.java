package com.duastore.service;

import com.duastore.dto.FlashSaleFormDTO;
import com.duastore.dto.FlashSaleItemFormDTO;
import com.duastore.model.Category;
import com.duastore.model.FlashSale;
import com.duastore.model.FlashSaleItem;
import com.duastore.model.Product;
import com.duastore.model.ProductVariant;
import com.duastore.repository.CategoryRepository;
import com.duastore.repository.FlashSaleItemRepository;
import com.duastore.repository.FlashSaleRepository;
import com.duastore.repository.ProductRepository;
import com.duastore.repository.ProductVariantRepository;
import com.duastore.service.admin.FlashSaleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FlashSaleServiceTest {

    @Autowired
    private FlashSaleService flashSaleService;

    @Autowired
    private FlashSaleRepository flashSaleRepository;

    @Autowired
    private FlashSaleItemRepository flashSaleItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository variantRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Product product;

    private ProductVariant variant;

    @BeforeEach
    void setUp() {
        Category cat = new Category();
        cat.setTenDanhMuc("Flash Sale Cat");
        cat.setActive(true);
        cat = categoryRepository.save(cat);

        product = new Product();
        product.setTenSanPham("Flash Sale Product");
        product.setTrangThaiSanPham("DANG_BAN");
        product.setActive(true);
        product.setDanhMucId(cat.getId());
        product = productRepository.save(product);

        variant = new ProductVariant();
        variant.setProductId(product.getId());
        variant.setTenBienThe("Flash Sale Variant");
        variant.setGiaGoc(new BigDecimal("100000"));
        variant.setSoLuongTon(1000);
        variant.setActive(true);
        variant = variantRepository.save(variant);
    }

    @Test
    void getAll_returnsAllFlashSales() {
        FlashSaleFormDTO dto = createDTO();
        flashSaleService.save(dto);
        flashSaleService.save(createDTO());

        List<FlashSale> list = flashSaleService.getAll();
        assertThat(list).hasSize(2);
    }

    @Test
    void save_createsFlashSale() {
        FlashSaleFormDTO dto = createDTO();

        FlashSale saved = flashSaleService.save(dto);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getIsActive()).isTrue();

        List<FlashSaleItem> items = flashSaleItemRepository.findByFlashSaleId(saved.getId());
        assertThat(items).hasSize(1);
        FlashSaleItem item = items.get(0);
        assertThat(item.getVariantId()).isEqualTo(variant.getId());
        assertThat(item.getGiaSale()).isEqualByComparingTo(new BigDecimal("70000"));
        assertThat(item.getSoLuongDaBan()).isZero();
        assertThat(item.getSoLuongToiDa()).isEqualTo(100);
    }

    @Test
    void save_updateExistingFlashSale() {
        FlashSale created = flashSaleService.save(createDTO());

        FlashSaleFormDTO updateDTO = new FlashSaleFormDTO();
        updateDTO.setId(created.getId());
        updateDTO.setTenChuongTrinh("Updated Flash Sale");
        updateDTO.setNgayBatDau(LocalDateTime.now().plusDays(2));
        updateDTO.setNgayKetThuc(LocalDateTime.now().plusDays(12));
        updateDTO.setIsActive(false);

        FlashSale updated = flashSaleService.save(updateDTO);

        assertThat(updated.getId()).isEqualTo(created.getId());
        assertThat(updated.getIsActive()).isFalse();
        assertThat(updated.getTenChuongTrinh()).isEqualTo("Updated Flash Sale");
    }

    @Test
    void save_variantNotFound_throwsException() {
        FlashSaleFormDTO dto = createDTO();
        dto.getItems().get(0).setVariantId(99999);

        assertThatThrownBy(() -> flashSaleService.save(dto))
                .hasMessageContaining("Biến thể không tồn tại");
    }

    @Test
    void save_ngayBatDauAfterNgayKetThuc_throwsException() {
        FlashSaleFormDTO dto = createDTO();
        dto.setNgayBatDau(LocalDateTime.now().plusDays(10));
        dto.setNgayKetThuc(LocalDateTime.now().plusDays(5));

        assertThatThrownBy(() -> flashSaleService.save(dto))
                .hasMessageContaining("Ngày bắt đầu phải trước ngày kết thúc");
    }

    @Test
    void getById_existingId_returnsFlashSale() {
        FlashSale saved = flashSaleService.save(createDTO());

        FlashSale found = flashSaleService.getById(saved.getId());

        assertThat(found.getId()).isEqualTo(saved.getId());
    }

    @Test
    void getById_nonExistingId_throwsException() {
        assertThatThrownBy(() -> flashSaleService.getById(99999))
                .hasMessageContaining("Không tìm thấy Flash Sale");
    }

    @Test
    void delete_removesFlashSale() {
        FlashSale saved = flashSaleService.save(createDTO());

        flashSaleService.delete(saved.getId());

        assertThat(flashSaleRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    void toggleActive_flipsIsActive() {
        FlashSale saved = flashSaleService.save(createDTO());
        assertThat(saved.getIsActive()).isTrue();

        flashSaleService.toggleActive(saved.getId());
        assertThat(flashSaleService.getById(saved.getId()).getIsActive()).isFalse();

        flashSaleService.toggleActive(saved.getId());
        assertThat(flashSaleService.getById(saved.getId()).getIsActive()).isTrue();
    }

    private FlashSaleFormDTO createDTO() {
        FlashSaleFormDTO dto = new FlashSaleFormDTO();
        dto.setTenChuongTrinh("Flash Sale Test");
        dto.setNgayBatDau(LocalDateTime.now().plusDays(1));
        dto.setNgayKetThuc(LocalDateTime.now().plusDays(7));
        dto.setIsActive(true);

        FlashSaleItemFormDTO item = new FlashSaleItemFormDTO();
        item.setVariantId(variant.getId());
        item.setGiaSale(new BigDecimal("70000"));
        item.setSoLuongToiDa(100);
        dto.getItems().add(item);
        return dto;
    }
}