package com.duastore.service.admin;

import com.duastore.model.StoreInfo;
import com.duastore.repository.StoreInfoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AdminStoreInfoService {

    private final StoreInfoRepository repository;

    public AdminStoreInfoService(StoreInfoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<StoreInfo> findAll() {
        return repository.findByIsActiveTrueOrderByIdAsc();
    }

    @Transactional(readOnly = true)
    public StoreInfo findById(Integer id) {
        return repository.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public StoreInfo findDefault() {
        return repository.findByIsDefaultTrueAndIsActiveTrue().orElse(null);
    }

    public StoreInfo save(StoreInfo info) {
        if (Boolean.TRUE.equals(info.getIsDefault())) {
            List<StoreInfo> all = repository.findAll();
            for (StoreInfo s : all) {
                if (!s.getId().equals(info.getId()) && Boolean.TRUE.equals(s.getIsDefault())) {
                    s.setIsDefault(false);
                    repository.save(s);
                }
            }
        }
        if (info.getId() == null) {
            if (repository.count() == 0) {
                info.setIsDefault(true);
            }
        }
        return repository.save(info);
    }

    public void delete(Integer id) {
        StoreInfo info = repository.findById(id).orElse(null);
        if (info != null) {
            info.setIsActive(false);
            repository.save(info);
        }
    }
}
