package com.duastore.service;

import com.duastore.model.Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
/**
 * Service chứa nghiệp vụ (business logic) xử lý định vị tọa độ địa lý.
 */
public class GeocodingService {

    private static final Logger log = LoggerFactory.getLogger(GeocodingService.class);
    private static final String NOMINATIM_SEARCH = "https://nominatim.openstreetmap.org/search?q=%s&format=json&limit=1";
    private static final long RATE_LIMIT_MS = 1100;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private volatile long lastRequestTime = 0;
    private final Map<String, double[]> cache = new ConcurrentHashMap<>();

    public void geocodeIfMissing(Address address) {
        if (address.getLatitude() != null && address.getLongitude() != null) {
            return;
        }

        String query = buildQuery(address);
        if (query.isBlank()) {
            return;
        }

        double[] cached = cache.get(query);
        if (cached != null) {
            address.setLatitude(cached[0]);
            address.setLongitude(cached[1]);
            return;
        }

        long now = System.currentTimeMillis();
        long wait = RATE_LIMIT_MS - (now - lastRequestTime);
        if (wait > 0) {
            try {
                Thread.sleep(wait);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }

        try {
            String encoded = URLEncoder.encode(query + ", Việt Nam", StandardCharsets.UTF_8);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(String.format(NOMINATIM_SEARCH, encoded)))
                    .header("User-Agent", "DuaStore/1.0")
                    .GET()
                    .build();
            lastRequestTime = System.currentTimeMillis();
            HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            String body = res.body();

            int latIdx = body.indexOf("\"lat\":\"");
            int lonIdx = body.indexOf("\"lon\":\"");
            if (latIdx >= 0 && lonIdx >= 0) {
                int latEnd = body.indexOf("\"", latIdx + 7);
                int lonEnd = body.indexOf("\"", lonIdx + 7);
                if (latEnd > 0 && lonEnd > 0) {
                    double lat = Double.parseDouble(body.substring(latIdx + 7, latEnd));
                    double lon = Double.parseDouble(body.substring(lonIdx + 7, lonEnd));
                    address.setLatitude(lat);
                    address.setLongitude(lon);
                    cache.put(query, new double[]{lat, lon});
                }
            }
        } catch (Exception e) {
            log.warn("Geocoding failed for query: {}", query, e);
        }
    }

    private String buildQuery(Address a) {
        StringBuilder sb = new StringBuilder();
        if (a.getDiaChiCuThe() != null) {
            sb.append(a.getDiaChiCuThe()).append(", ");
        }
        if (a.getPhuongXa() != null) {
            sb.append(a.getPhuongXa()).append(", ");
        }
        if (a.getQuanHuyen() != null) {
            sb.append(a.getQuanHuyen()).append(", ");
        }
        if (a.getTinhThanh() != null) {
            sb.append(a.getTinhThanh());
        }
        return sb.toString().replaceAll(", $", "");
    }
}
