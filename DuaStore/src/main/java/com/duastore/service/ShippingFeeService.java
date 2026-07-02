package com.duastore.service;

import com.duastore.model.Address;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class ShippingFeeService {

    private static final BigDecimal BASE_FEE = new BigDecimal("10000");
    private static final BigDecimal PER_KM = new BigDecimal("200");
    private static final BigDecimal MIN_FEE = new BigDecimal("10000");
    private static final BigDecimal MAX_FEE = new BigDecimal("1000000");
    private static final double EARTH_RADIUS_KM = 6371.0;

    private final double storeLat;
    private final double storeLng;

    public ShippingFeeService(
            @Value("${store.latitude}") double storeLat,
            @Value("${store.longitude}") double storeLng) {
        this.storeLat = storeLat;
        this.storeLng = storeLng;
    }

    public BigDecimal calculateFee(Address address, String shippingMethod) {
        if (address.getLatitude() == null || address.getLongitude() == null) {
            return fallbackFee(shippingMethod);
        }
        return calculateFee(address.getLatitude(), address.getLongitude(), shippingMethod);
    }

    public BigDecimal calculateFee(double userLat, double userLng, String shippingMethod) {
        if ("NHAN_TAI_CONG".equalsIgnoreCase(shippingMethod)) {
            return BigDecimal.ZERO;
        }
        double distance = haversine(userLat, userLng, storeLat, storeLng);
        BigDecimal fee = BASE_FEE.add(PER_KM.multiply(BigDecimal.valueOf(distance)));
        if (fee.compareTo(MIN_FEE) < 0) fee = MIN_FEE;
        if (fee.compareTo(MAX_FEE) > 0) fee = MAX_FEE;
        return fee.setScale(0, RoundingMode.HALF_UP);
    }

    private BigDecimal fallbackFee(String shippingMethod) {
        if ("NHAN_TAI_CONG".equalsIgnoreCase(shippingMethod)) {
            return BigDecimal.ZERO;
        }
        return MIN_FEE;
    }

    public double getStoreLat() {
        return storeLat;
    }

    public double getStoreLng() {
        return storeLng;
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }
}
