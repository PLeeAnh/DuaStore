package com.duastore.service;

import com.duastore.dto.CarrierQuote;
import com.duastore.model.Address;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
/**
 * Service chứa nghiệp vụ (business logic) xử lý nhiều đơn vị vận chuyển, vận chuyển.
 */
public class MultiCarrierShippingService {

    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final BigDecimal ABSOLUTE_MAX = new BigDecimal("1000000");

    private final SiteSettingService siteSettingService;
    private final GHNShippingService ghnShippingService;
    private final double storeLat;
    private final double storeLng;

    public MultiCarrierShippingService(
            SiteSettingService siteSettingService,
            GHNShippingService ghnShippingService,
            @Value("${store.latitude}") double storeLat,
            @Value("${store.longitude}") double storeLng) {
        this.siteSettingService = siteSettingService;
        this.ghnShippingService = ghnShippingService;
        this.storeLat = storeLat;
        this.storeLng = storeLng;
    }

    public List<CarrierQuote> getQuotes(Address address, BigDecimal subtotal) {
        List<CarrierQuote> quotes = new ArrayList<>();
        BigDecimal freeThreshold = freeThreshold();
        boolean ghnEnabled = "1".equals(siteSettingService.getValue("carrier_ghn_enabled", "1"));
        boolean ghtkEnabled = "1".equals(siteSettingService.getValue("carrier_ghtk_enabled", "1"));
        if (subtotal != null && subtotal.compareTo(freeThreshold) >= 0) {
            if (ghnEnabled) quotes.add(new CarrierQuote("GHN", "Giao Hàng Nhanh", BigDecimal.ZERO, deliveryDays("GHN"), false));
            if (ghtkEnabled) quotes.add(new CarrierQuote("GHTK", "Giao Hàng Tiết Kiệm", BigDecimal.ZERO, deliveryDays("GHTK"), false));
            return quotes;
        }
        if (ghnEnabled) {
            BigDecimal ghnFee = ghnShippingService.calculateFee(address);
            if (ghnFee != null) {
                quotes.add(new CarrierQuote("GHN", "Giao Hàng Nhanh", ghnFee, deliveryDays("GHN"), false));
            } else {
                quotes.add(estimateQuote("GHN", "Giao Hàng Nhanh", address));
            }
        }
        if (ghtkEnabled) {
            quotes.add(estimateQuote("GHTK", "Giao Hàng Tiết Kiệm", address));
        }
        return quotes;
    }

    public CarrierQuote getQuoteForCarrier(String carrierCode, Address address, BigDecimal subtotal) {
        BigDecimal freeThreshold = freeThreshold();
        if (subtotal != null && subtotal.compareTo(freeThreshold) >= 0) {
            return new CarrierQuote(carrierCode, carrierName(carrierCode), BigDecimal.ZERO, deliveryDays(carrierCode), false);
        }
        if ("GHN".equals(carrierCode)) {
            BigDecimal ghnFee = ghnShippingService.calculateFee(address);
            if (ghnFee != null) {
                return new CarrierQuote(carrierCode, carrierName(carrierCode), ghnFee, deliveryDays(carrierCode), false);
            }
        }
        return estimateQuote(carrierCode, carrierName(carrierCode), address);
    }

    private CarrierQuote estimateQuote(String code, String name, Address address) {
        if (address.getLatitude() == null || address.getLongitude() == null) {
            BigDecimal minFee = carrierMinFee(code);
            return new CarrierQuote(code, name, minFee, deliveryDays(code), true);
        }
        double distance = haversine(address.getLatitude(), address.getLongitude(), storeLat, storeLng);
        BigDecimal baseFee = carrierBaseFee(code);
        BigDecimal rateKm = carrierRateKm(code);
        BigDecimal fee = baseFee.add(rateKm.multiply(BigDecimal.valueOf(distance)));
        BigDecimal minFee = carrierMinFee(code);
        if (fee.compareTo(minFee) < 0) { fee = minFee; }
        if (fee.compareTo(ABSOLUTE_MAX) > 0) { fee = ABSOLUTE_MAX; }
        return new CarrierQuote(code, name, fee.setScale(0, RoundingMode.HALF_UP), deliveryDays(code), true);
    }

    public BigDecimal calculateFeeForCarrier(String carrierCode, Address address, BigDecimal subtotal) {
        CarrierQuote q = getQuoteForCarrier(carrierCode, address, subtotal);
        return q.getFee();
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

    private String carrierName(String code) {
        return "GHN".equals(code) ? "Giao Hàng Nhanh" : "Giao Hàng Tiết Kiệm";
    }

    private BigDecimal carrierBaseFee(String code) {
        String v = siteSettingService.getValue("carrier_" + code.toLowerCase() + "_base_fee", "");
        if (!v.isBlank()) return new BigDecimal(v);
        return "GHN".equals(code) ? new BigDecimal("15000") : new BigDecimal("12000");
    }

    private BigDecimal carrierRateKm(String code) {
        String v = siteSettingService.getValue("carrier_" + code.toLowerCase() + "_rate_km", "");
        if (!v.isBlank()) return new BigDecimal(v);
        return "GHN".equals(code) ? new BigDecimal("220") : new BigDecimal("200");
    }

    private BigDecimal carrierMinFee(String code) {
        String v = siteSettingService.getValue("carrier_" + code.toLowerCase() + "_min_fee", "");
        if (!v.isBlank()) return new BigDecimal(v);
        return new BigDecimal("10000");
    }

    private int deliveryDays(String code) {
        String key = "carrier_" + code.toLowerCase() + "_days";
        String v = siteSettingService.getValue(key, "");
        if (!v.isBlank()) {
            try { return Integer.parseInt(v); } catch (NumberFormatException ignored) {}
        }
        return "GHN".equals(code) ? 3 : 4;
    }

    private BigDecimal freeThreshold() {
        String v = siteSettingService.getValue("shipping_free_min", "500000");
        if (v != null && !v.isBlank()) {
            try { return new BigDecimal(v.trim()); } catch (NumberFormatException ignored) {}
        }
        return new BigDecimal("500000");
    }

}
