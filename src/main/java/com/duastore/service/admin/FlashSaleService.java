package com.duastore.service.admin;

import com.duastore.dto.FlashSaleFormDTO;
import com.duastore.dto.FlashSaleItemFormDTO;
import com.duastore.model.FlashSale;
import com.duastore.model.FlashSaleItem;
import com.duastore.model.ProductVariant;
import com.duastore.repository.FlashSaleItemRepository;
import com.duastore.repository.FlashSaleRepository;
import com.duastore.repository.ProductVariantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
/**
 * Service chứa nghiệp vụ (business logic) xử lý flash sale (giảm giá chớp nhoáng).
 */
public class FlashSaleService {

    private final FlashSaleRepository flashSaleRepository;
    private final FlashSaleItemRepository flashSaleItemRepository;
    private final ProductVariantRepository variantRepository;

    public FlashSaleService(FlashSaleRepository flashSaleRepository,
            FlashSaleItemRepository flashSaleItemRepository,
            ProductVariantRepository variantRepository) {
        this.flashSaleRepository = flashSaleRepository;
        this.flashSaleItemRepository = flashSaleItemRepository;
        this.variantRepository = variantRepository;
    }

    @Transactional(readOnly = true)
    public List<FlashSale> getAll() {
        return flashSaleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public FlashSale getById(Integer id) {
        return flashSaleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Flash Sale"));
    }

    public FlashSale save(FlashSaleFormDTO dto) {
        if (dto.getNgayBatDau() != null && dto.getNgayKetThuc() != null
                && !dto.getNgayBatDau().isBefore(dto.getNgayKetThuc())) {
            throw new RuntimeException("Ngày bắt đầu phải trước ngày kết thúc");
        }
        FlashSale fs = dto.getId() != null ? getById(dto.getId()) : new FlashSale();
        fs.setTenChuongTrinh(dto.getTenChuongTrinh());
        fs.setMoTa(dto.getMoTa());
        fs.setNgayBatDau(dto.getNgayBatDau());
        fs.setNgayKetThuc(dto.getNgayKetThuc());
        fs.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        fs.setPriority(dto.getPriority() != null ? dto.getPriority() : 0);
        FlashSale saved = flashSaleRepository.save(fs);
        if (dto.getItems() != null) {
            for (FlashSaleItemFormDTO itemDto : dto.getItems()) {
                if (itemDto.getVariantId() == null) {
                    continue;
                }
                itemDto.setFlashSaleId(saved.getId());
                addItem(itemDto);
            }
        }
        return saved;
    }

    public FlashSaleItem addItem(FlashSaleItemFormDTO dto) {
        FlashSale fs = getById(dto.getFlashSaleId());
        ProductVariant variant = variantRepository.findById(dto.getVariantId())
                .orElseThrow(() -> new RuntimeException("Biến thể không tồn tại"));
        boolean exists = flashSaleItemRepository.findByFlashSaleId(fs.getId()).stream()
                .anyMatch(i -> i.getVariantId().equals(dto.getVariantId())
                        && Boolean.TRUE.equals(i.getIsActive()));
        if (exists) {
            throw new RuntimeException("Biến thể này đã có trong Flash Sale");
        }
        BigDecimal giaGoc = variant.getGiaGoc() != null ? variant.getGiaGoc() : BigDecimal.ZERO;
        if (dto.getGiaSale() == null || dto.getGiaSale().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Giá sale không được âm");
        }
        if (dto.getGiaSale().compareTo(giaGoc) > 0) {
            throw new RuntimeException("Giá sale không được lớn hơn giá gốc");
        }
        FlashSaleItem item = new FlashSaleItem();
        item.setFlashSale(fs);
        item.setVariantId(dto.getVariantId());
        item.setGiaGoc(giaGoc);
        item.setGiaSale(dto.getGiaSale());
        item.setSoLuongToiDa(dto.getSoLuongToiDa() != null ? dto.getSoLuongToiDa() : 0);
        item.setSoLuongDaBan(0);
        item.setIsActive(true);
        return flashSaleItemRepository.save(item);
    }

    public void deleteItem(Integer itemId) {
        FlashSaleItem item = flashSaleItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm flash sale"));
        flashSaleItemRepository.delete(item);
    }

    public void toggleItem(Integer itemId) {
        FlashSaleItem item = flashSaleItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm flash sale"));
        item.setIsActive(!Boolean.TRUE.equals(item.getIsActive()));
        flashSaleItemRepository.save(item);
    }

    public void delete(Integer id) {
        FlashSale fs = getById(id);
        flashSaleRepository.delete(fs);
    }

    public void toggleActive(Integer id) {
        FlashSale fs = getById(id);
        fs.setIsActive(!Boolean.TRUE.equals(fs.getIsActive()));
        flashSaleRepository.save(fs);
    }
}