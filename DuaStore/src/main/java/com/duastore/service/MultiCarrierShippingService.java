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

    public List<CarrierQuote> getQuotes(Address address, String shippingMethod, BigDecimal subtotal) {
        List<CarrierQuote> quotes = new ArrayList<>();
        BigDecimal freeThreshold = new BigDecimal(siteSettingService.getValue("shipping_free_min", "500000"));
        if (subtotal != null && subtotal.compareTo(freeThreshold) >= 0) {
            for (String code : new String[]{"GHN", "GHTK", "VIETTEL_POST"}) {
                String name = carrierName(code);
                quotes.add(new CarrierQuote(code, name, BigDecimal.ZERO, deliveryDays(code, shippingMethod), false));
            }
            return quotes;
        }
        for (String code : new String[]{"GHN", "GHTK", "VIETTEL_POST"}) {
            String name = carrierName(code);
            CarrierQuote q;
            if ("GHN".equals(code)) {
                BigDecimal ghnFee = ghnShippingService.calculateFee(address, shippingMethod);
                if (ghnFee != null) {
                    q = new CarrierQuote(code, name, ghnFee, deliveryDays(code, shippingMethod), false);
                } else {
                    q = estimateQuote(code, name, address, shippingMethod);
                }
            } else {
                q = estimateQuote(code, name, address, shippingMethod);
            }
            quotes.add(q);
        }
        return quotes;
    }

    public CarrierQuote getQuoteForCarrier(String carrierCode, Address address, String shippingMethod, BigDecimal subtotal) {
        BigDecimal freeThreshold = new BigDecimal(siteSettingService.getValue("shipping_free_min", "500000"));
        if (subtotal != null && subtotal.compareTo(freeThreshold) >= 0) {
            return new CarrierQuote(carrierCode, carrierName(carrierCode), BigDecimal.ZERO, deliveryDays(carrierCode, shippingMethod), false);
        }
        if ("GHN".equals(carrierCode)) {
            BigDecimal ghnFee = ghnShippingService.calculateFee(address, shippingMethod);
            if (ghnFee != null) {
                return new CarrierQuote(carrierCode, carrierName(carrierCode), ghnFee, deliveryDays(carrierCode, shippingMethod), false);
            }
        }
        return estimateQuote(carrierCode, carrierName(carrierCode), address, shippingMethod);
    }

    private CarrierQuote estimateQuote(String code, String name, Address address, String shippingMethod) {
        if (address.getLatitude() == null || address.getLongitude() == null) {
            BigDecimal minFee = carrierMinFee(code);
            return new CarrierQuote(code, name, minFee, deliveryDays(code, shippingMethod), true);
        }
        double distance = haversine(address.getLatitude(), address.getLongitude(), storeLat, storeLng);
        BigDecimal baseFee = carrierBaseFee(code);
        BigDecimal rateKm = carrierRateKm(code);
        BigDecimal fee = baseFee.add(rateKm.multiply(BigDecimal.valueOf(distance)));
        BigDecimal minFee = carrierMinFee(code);
        if (fee.compareTo(minFee) < 0) { fee = minFee; }
        if (fee.compareTo(ABSOLUTE_MAX) > 0) { fee = ABSOLUTE_MAX; }
        return new CarrierQuote(code, name, fee.setScale(0, RoundingMode.HALF_UP), deliveryDays(code, shippingMethod), true);
    }

    public BigDecimal calculateFeeForCarrier(String carrierCode, Address address, String shippingMethod, BigDecimal subtotal) {
        CarrierQuote q = getQuoteForCarrier(carrierCode, address, shippingMethod, subtotal);
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
        return switch (code) {
            case "GHN" -> "Giao Hàng Nhanh";
            case "GHTK" -> "Giao Hàng Tiết Kiệm";
            case "VIETTEL_POST" -> "Viettel Post";
            default -> code;
        };
    }

    private BigDecimal carrierBaseFee(String code) {
        String v = siteSettingService.getValue("carrier_" + code.toLowerCase() + "_base_fee", "");
        if (!v.isBlank()) return new BigDecimal(v);
        return switch (code) {
            case "GHN" -> new BigDecimal("15000");
            case "GHTK" -> new BigDecimal("12000");
            case "VIETTEL_POST" -> new BigDecimal("10000");
            default -> new BigDecimal("10000");
        };
    }

    private BigDecimal carrierRateKm(String code) {
        String v = siteSettingService.getValue("carrier_" + code.toLowerCase() + "_rate_km", "");
        if (!v.isBlank()) return new BigDecimal(v);
        return switch (code) {
            case "GHN" -> new BigDecimal("220");
            case "GHTK" -> new BigDecimal("200");
            case "VIETTEL_POST" -> new BigDecimal("170");
            default -> new BigDecimal("200");
        };
    }

    private BigDecimal carrierMinFee(String code) {
        String v = siteSettingService.getValue("carrier_" + code.toLowerCase() + "_min_fee", "");
        if (!v.isBlank()) return new BigDecimal(v);
        return switch (code) {
            default -> new BigDecimal("10000");
        };
    }

    private int deliveryDays(String code, String shippingMethod) {
        String key = "carrier_" + code.toLowerCase() + "_days_" + (shippingMethod != null ? shippingMethod.toLowerCase() : "ship");
        String v = siteSettingService.getValue(key, "");
        if (!v.isBlank()) {
            try { return Integer.parseInt(v); } catch (NumberFormatException ignored) {}
        }
        boolean express = "EXPRESS".equals(shippingMethod);
        return switch (code) {
            case "GHN" -> express ? 2 : 5;
            case "GHTK" -> express ? 3 : 5;
            case "VIETTEL_POST" -> express ? 3 : 5;
            default -> express ? 4 : 7;
        };
    }

}
