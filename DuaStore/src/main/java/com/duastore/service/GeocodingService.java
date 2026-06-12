package com.duastore.service;

import com.duastore.model.Address;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Service
public class GeocodingService {

    private static final String NOMINATIM_SEARCH = "https://nominatim.openstreetmap.org/search?q=%s&format=json&limit=1";
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public void geocodeIfMissing(Address address) {
        if (address.getLatitude() != null && address.getLongitude() != null) return;

        String query = buildQuery(address);
        if (query.isBlank()) return;

        try {
            String encoded = URLEncoder.encode(query + ", Việt Nam", StandardCharsets.UTF_8);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(String.format(NOMINATIM_SEARCH, encoded)))
                    .header("User-Agent", "DuaStore/1.0")
                    .GET()
                    .build();
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
                }
            }
        } catch (Exception ignored) {
        }
    }

    private String buildQuery(Address a) {
        StringBuilder sb = new StringBuilder();
        if (a.getDiaChiCuThe() != null) sb.append(a.getDiaChiCuThe()).append(", ");
        if (a.getPhuongXa() != null) sb.append(a.getPhuongXa()).append(", ");
        if (a.getQuanHuyen() != null) sb.append(a.getQuanHuyen()).append(", ");
        if (a.getTinhThanh() != null) sb.append(a.getTinhThanh());
        return sb.toString().replaceAll(", $", "");
    }
}
