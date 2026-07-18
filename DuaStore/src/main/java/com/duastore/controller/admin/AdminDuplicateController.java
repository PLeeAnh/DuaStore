package com.duastore.controller.admin;

import com.duastore.service.DuplicateDetectionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/api")
public class AdminDuplicateController {

    private final DuplicateDetectionService duplicateDetectionService;

    public AdminDuplicateController(DuplicateDetectionService duplicateDetectionService) {
        this.duplicateDetectionService = duplicateDetectionService;
    }

    @GetMapping("/kiem-tra-trung")
    public Map<String, Object> checkDuplicate(
            @RequestParam String type,
            @RequestParam String name,
            @RequestParam(required = false) Integer productId,
            @RequestParam(required = false) Integer excludeId) {

        List<DuplicateDetectionService.DuplicateMatch> matches = switch (type) {
            case "product" -> duplicateDetectionService.findProductDuplicates(name, excludeId);
            case "variant" -> duplicateDetectionService.findVariantDuplicates(name, productId, excludeId);
            default -> duplicateDetectionService.findCategoryDuplicates(name, excludeId);
        };

        return Map.of(
                "hasDuplicates", !matches.isEmpty(),
                "matches", matches.stream()
                        .map(m -> Map.of(
                                "id", m.id(),
                                "name", m.name(),
                                "type", m.type(),
                                "score", m.formattedScore(),
                                "path", m.path()
                        ))
                        .toList()
        );
    }
}
