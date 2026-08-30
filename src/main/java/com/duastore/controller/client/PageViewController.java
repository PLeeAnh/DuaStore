package com.duastore.controller.client;

import com.duastore.model.PageView;
import com.duastore.repository.PageViewRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/pageview")
public class PageViewController {

    private final PageViewRepository pageViewRepository;

    public PageViewController(PageViewRepository pageViewRepository) {
        this.pageViewRepository = pageViewRepository;
    }

    @PostMapping("/track")
    public ResponseEntity<Map<String, Object>> track(@RequestBody Map<String, Object> body,
                                                     @RequestHeader(value = "X-Session-Id", required = false) String headerSession,
                                                     @RequestHeader(value = "User-Agent", required = false) String userAgent) {
        String sessionId = headerSession;
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = String.valueOf(System.nanoTime());
        }
        
        PageView pv = new PageView();
        pv.setSessionId(sessionId);
        pv.setEventType((String) body.getOrDefault("eventType", "PAGE_VIEW"));
        pv.setPagePath((String) body.get("pagePath"));
        if (body.get("productId") != null) {
            pv.setProductId(Integer.valueOf(String.valueOf(body.get("productId"))));
        }
        if (body.get("userId") != null) {
            pv.setUserId(Integer.valueOf(String.valueOf(body.get("userId"))));
        }
        pv.setMetadata((String) body.get("metadata"));
        
        pageViewRepository.save(pv);
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
