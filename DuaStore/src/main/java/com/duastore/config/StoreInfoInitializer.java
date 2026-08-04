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
        store.setDuong("Phố Tôn Thất Thuyết");
        store.setPhuongXa("Phan Bội Châu");
        store.setQuanHuyen("Phường Hồng Bàng");
        store.setTinhThanh("Thành phố Hải Phòng");
        store.setSoDienThoai("0983595240");
        store.setEmail("anhpltp00872@gmail.com");
        store.setLatitude(20.8565);
        store.setLongitude(106.6756);
        store.setIsActive(true);
        store.setIsDefault(true);
        repository.save(store);
        log.info("Created default store info: DuaStore");
    }
}
