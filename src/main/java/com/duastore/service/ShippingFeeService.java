package com.duastore.service;

import com.duastore.model.Address;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
/**
 * Service chứa nghiệp vụ (business logic) xử lý vận chuyển.
 */
public class ShippingFeeService {

    private static final BigDecimal MIN_FEE = new BigDecimal("10000");
    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final BigDecimal RATE_KM = new BigDecimal("200");

    private final double storeLat;
    private final double storeLng;
    private final GHNShippingService ghnShippingService;

    public ShippingFeeService(
            @Value("${store.latitude}") double storeLat,
            @Value("${store.longitude}") double storeLng,
            GHNShippingService ghnShippingService) {
        this.storeLat = storeLat;
        this.storeLng = storeLng;
        this.ghnShippingService = ghnShippingService;
    }

    public BigDecimal calculateFee(Address address, BigDecimal subtotal) {
        if (subtotal != null && subtotal.compareTo(new BigDecimal("500000")) >= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal ghnFee = ghnShippingService.calculateFee(address);
        if (ghnFee != null) {
            return ghnFee;
        }
        if (address.getLatitude() == null || address.getLongitude() == null) {
            return MIN_FEE;
        }
        return calculateFee(address.getLatitude(), address.getLongitude());
    }

    private BigDecimal calculateFee(double userLat, double userLng) {
        double distance = haversine(userLat, userLng, storeLat, storeLng);
        BigDecimal fee = MIN_FEE.add(RATE_KM.multiply(BigDecimal.valueOf(distance)));
        if (fee.compareTo(MIN_FEE) < 0) { fee = MIN_FEE; }
        if (fee.compareTo(new BigDecimal("1000000")) > 0) { fee = new BigDecimal("1000000"); }
        return fee.setScale(0, RoundingMode.HALF_UP);
    }

    public int getDeliveryDays() {
        return 5;
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
