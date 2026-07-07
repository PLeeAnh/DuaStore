package com.duastore.service.admin;

import com.duastore.dto.FlashSaleFormDTO;
import com.duastore.model.FlashSale;
import com.duastore.repository.FlashSaleRepository;
import com.duastore.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class FlashSaleService {

    private final FlashSaleRepository flashSaleRepository;
    private final ProductRepository productRepository;

    public FlashSaleService(FlashSaleRepository flashSaleRepository,
            ProductRepository productRepository) {
        this.flashSaleRepository = flashSaleRepository;
        this.productRepository = productRepository;
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
        if (!productRepository.existsById(dto.getProductId())) {
            throw new RuntimeException("Sản phẩm không tồn tại");
        }
        if (dto.getNgayBatDau() != null && dto.getNgayKetThuc() != null
                && !dto.getNgayBatDau().isBefore(dto.getNgayKetThuc())) {
            throw new RuntimeException("Ngày bắt đầu phải trước ngày kết thúc");
        }
        FlashSale fs = dto.getId() != null ? getById(dto.getId()) : new FlashSale();
        fs.setProductId(dto.getProductId());
        fs.setGiaTriGiam(dto.getGiaTriGiam());
        fs.setNgayBatDau(dto.getNgayBatDau());
        fs.setNgayKetThuc(dto.getNgayKetThuc());
        fs.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        if (dto.getSoLuongToiDa() != null) {
            fs.setSoLuongToiDa(dto.getSoLuongToiDa());
        }
        if (dto.getSoLuongDaBan() != null) {
            fs.setSoLuongDaBan(dto.getSoLuongDaBan());
        }
        return flashSaleRepository.save(fs);
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
