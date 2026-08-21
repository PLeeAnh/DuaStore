package com.duastore.service.admin;

import com.duastore.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
/**
 * Service chứa nghiệp vụ (business logic) xử lý auto description service.
 */
public class AutoDescriptionService {

    private final CategoryRepository categoryRepository;

    public AutoDescriptionService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public String generate(Map<String, String> params) {
        String name = params.getOrDefault("tenSanPham", "");
        String categoryId = params.getOrDefault("danhMucId", "");
        String brand = params.getOrDefault("thuongHieu", "");
        String material = params.getOrDefault("chatLieu", "");
        String origin = params.getOrDefault("xuatXu", "");
        String purpose = params.getOrDefault("mucDichSuDung", "");
        String glassType = params.getOrDefault("kinhLoai", "");
        String capacity = params.getOrDefault("dungTich", "");

        String categoryName = "";
        if (!categoryId.isEmpty()) {
            try {
                var cat = categoryRepository.findById(Integer.parseInt(categoryId));
                if (cat.isPresent()) categoryName = cat.get().getTenDanhMuc();
            } catch (Exception e) {
                // Category not found or parse error, leave empty
            }
        }

        StringBuilder sb = new StringBuilder();

        sb.append("<h4>").append(esc(name)).append("</h4>");
        sb.append("<p><strong>").append(esc(name)).append("</strong>");
        if (!categoryName.isEmpty()) sb.append(" thuộc dòng sản phẩm ").append(esc(categoryName));
        sb.append(", được thiết kế với tiêu chí chất lượng cao và thẩm mỹ tinh tế,");
        sb.append(" phù hợp với nhu cầu sử dụng hàng ngày của bạn.</p>");

        sb.append("<h5>Đặc điểm nổi bật:</h5><ul>");
        if (!brand.isEmpty()) sb.append("<li><strong>Thương hiệu:</strong> ").append(esc(brand)).append("</li>");
        if (!material.isEmpty()) sb.append("<li><strong>Chất liệu:</strong> ").append(esc(material)).append("</li>");
        if (!origin.isEmpty()) sb.append("<li><strong>Xuất xứ:</strong> ").append(esc(origin)).append("</li>");
        if (!glassType.isEmpty()) sb.append("<li><strong>Loại kính:</strong> ").append(esc(glassType)).append("</li>");
        if (!capacity.isEmpty()) sb.append("<li><strong>Dung tích:</strong> ").append(esc(capacity)).append("</li>");
        if (!purpose.isEmpty()) sb.append("<li><strong>Mục đích sử dụng:</strong> ").append(esc(purpose)).append("</li>");
        sb.append("<li><strong>Thiết kế sang trọng, hiện đại</strong>, phù hợp làm quà tặng ý nghĩa</li>");
        sb.append("<li><strong>Dễ dàng vệ sinh</strong> và bảo quản</li>");
        sb.append("</ul>");

        sb.append("<h5>Mô tả chi tiết:</h5>");
        sb.append("<p>Sản phẩm ").append(esc(name)).append(" là lựa chọn hoàn hảo cho những ai yêu thích sự tinh tế và chất lượng.");
        sb.append(" Với thiết kế được nghiên cứu kỹ lưỡng, sản phẩm không chỉ đáp ứng tốt nhu cầu sử dụng");
        sb.append(" mà còn là điểm nhấn cho không gian của bạn.</p>");

        sb.append("<h5>Hướng dẫn bảo quản:</h5><ul>");
        sb.append("<li>Vệ sinh nhẹ nhàng bằng khăn mềm</li>");
        sb.append("<li>Tránh va đập mạnh</li>");
        sb.append("<li>Bảo quản nơi khô ráo, thoáng mát</li>");
        sb.append("</ul>");

        sb.append("<p style=\"color:#666;font-style:italic\">");
        sb.append("📦 Giao hàng toàn quốc | 🔄 Đổi trả trong 7 ngày | 💳 Thanh toán khi nhận hàng");
        sb.append("</p>");

        return sb.toString();
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;");
    }
}
