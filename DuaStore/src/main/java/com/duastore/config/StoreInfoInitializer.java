package com.duastore.config;

import com.duastore.model.StoreInfo;
import com.duastore.repository.StoreInfoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class StoreInfoInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(StoreInfoInitializer.class);
    private final StoreInfoRepository repository;

    public StoreInfoInitializer(StoreInfoRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }

        StoreInfo store = new StoreInfo();
        store.setTenCuaHang("DuaStore");
        store.setSoNha("Số 1");
        store.setDuong("Đại Cồ Việt");
        store.setPhuongXa("Phường Bách Khoa");
        store.setQuanHuyen("Quận Hai Bà Trưng");
        store.setTinhThanh("Thành phố Hà Nội");
        store.setSoDienThoai("0901 234 567");
        store.setEmail("hello@dua.store");
        store.setLatitude(21.028511);
        store.setLongitude(105.854170);
        store.setIsActive(true);
        store.setIsDefault(true);
        repository.save(store);
        log.info("Created default store info: DuaStore");
    }
}
