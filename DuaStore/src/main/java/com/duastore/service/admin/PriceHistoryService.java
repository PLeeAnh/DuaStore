package com.duastore.service.admin;

import com.duastore.model.PriceHistory;
import com.duastore.repository.PriceHistoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class PriceHistoryService {

    private final PriceHistoryRepository priceHistoryRepository;

    public PriceHistoryService(PriceHistoryRepository priceHistoryRepository) {
        this.priceHistoryRepository = priceHistoryRepository;
    }

    public void record(Integer variantId, String variantName, Integer productId, String productName,
                       BigDecimal oldPrice, BigDecimal newPrice, Integer adminId, String source) {
        if (oldPrice != null && oldPrice.compareTo(newPrice) == 0) {
            return;
        }
        PriceHistory ph = new PriceHistory();
        ph.setVariantId(variantId);
        ph.setVariantName(variantName);
        ph.setProductId(productId);
        ph.setProductName(productName);
        ph.setGiaCu(oldPrice);
        ph.setGiaMoi(newPrice);
        ph.setNguoiThayDoiId(adminId);
        ph.setNguon(source);
        priceHistoryRepository.save(ph);
    }

    @Transactional(readOnly = true)
    public List<PriceHistory> getByVariant(Integer variantId) {
        return priceHistoryRepository.findByVariantIdOrderByNgayThayDoiDesc(variantId);
    }

    @Transactional(readOnly = true)
    public List<PriceHistory> getByProduct(Integer productId) {
        return priceHistoryRepository.findByProductIdOrderByNgayThayDoiDesc(productId);
    }

    @Transactional(readOnly = true)
    public Page<PriceHistory> getAllPaged(Pageable pageable) {
        return priceHistoryRepository.findAllByOrderByNgayThayDoiDesc(pageable);
    }
}
