package com.duastore.service;

import com.duastore.model.ContactMessage;
import com.duastore.repository.ContactMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.duastore.model.ContactMessage.*;

@Service
public class ContactMessageService {

    private final ContactMessageRepository contactMessageRepository;

    public ContactMessageService(ContactMessageRepository contactMessageRepository) {
        this.contactMessageRepository = contactMessageRepository;
    }

    private static final Map<String, List<String>> CATEGORY_KEYWORDS = new LinkedHashMap<>();

    static {
        CATEGORY_KEYWORDS.put(LOAI_KHIEU_NAI, List.of("khieu nai", "phan nan", "lua doi", "vo", "hu hang",
                "hong hang", "sai san pham", "khong dung", "gap loi", "khong hai long"));
        CATEGORY_KEYWORDS.put(LOAI_DON_HANG, List.of("don hang", "dat hang", "ban hang", "ma don", "huy don",
                "mua", "chua nhan", "kiem tra don", "theo doi don", "tracking", "trang thai don", "da hoan",
                "cho xac nhan", "thu hoi hang"));
        CATEGORY_KEYWORDS.put(LOAI_GIAO_HANG, List.of("giao hang", "van chuyen", "ship", "phat nhanh",
                "chuyen phat", "nhan hang", "ngay giao", "phi giao", "bo cung"));
        CATEGORY_KEYWORDS.put(LOAI_THANH_TOAN, List.of("thanh toan", "chuyen khoan", "cod", "hoa don",
                "tra tien", "tien", "hoan tien", "voucher", "giam gia"));
        CATEGORY_KEYWORDS.put(LOAI_HOP_TAC, List.of("hop tac", "dai ly", "phan phoi", "ban buon", "ban si",
                "doi tac", "nguon hang", "nhap hang"));
        CATEGORY_KEYWORDS.put(LOAI_SAN_PHAM, List.of("san pham", "con hang", "het hang", "kich thuoc", "size",
                "mau sac", "thiet ke", "chat lieu", "bang gia", "phu kien", "men", "chau", "ly"));
    }

    private static final List<String> SPAM_KEYWORDS = List.of(
            "http", "www.", "://", "casino", "ca do", "cado", "tang follow", "tang like",
            "mua view", "btc", "bitcoin", "crypto", "nap the", "jackpot", "danh bac");

    @Transactional
    public ContactMessage save(String hoTen, String email, String noiDung) {
        Classification c = classify(noiDung);
        ContactMessage m = new ContactMessage();
        m.setHoTen(hoTen);
        m.setEmail(email);
        m.setNoiDung(noiDung);
        m.setPhanLoai(c.phanLoai());
        m.setIsSpam(c.isSpam());
        m.setIsRead(false);
        return contactMessageRepository.save(m);
    }

    public Classification classify(String noiDung) {
        String text = normalize(noiDung == null ? "" : noiDung);
        for (String kw : SPAM_KEYWORDS) {
            if (text.contains(kw)) {
                return new Classification(LOAI_KHAC, true);
            }
        }
        int bestScore = 0;
        String bestCode = LOAI_KHAC;
        for (Map.Entry<String, List<String>> entry : CATEGORY_KEYWORDS.entrySet()) {
            int score = 0;
            for (String kw : entry.getValue()) {
                if (text.contains(kw)) {
                    score++;
                }
            }
            if (score > bestScore) {
                bestScore = score;
                bestCode = entry.getKey();
            }
        }
        return new Classification(bestScore > 0 ? bestCode : LOAI_KHAC, false);
    }

    public List<ContactMessage> findAll() {
        return contactMessageRepository.findAllByOrderByCreatedAtDesc();
    }

    public ContactMessage getById(Integer id) {
        return contactMessageRepository.findById(id).orElse(null);
    }

    public ContactMessage toggleRead(Integer id) {
        ContactMessage m = getById(id);
        if (m != null) {
            m.setIsRead(!Boolean.TRUE.equals(m.getIsRead()));
            return contactMessageRepository.save(m);
        }
        return null;
    }

    public boolean delete(Integer id) {
        ContactMessage m = getById(id);
        if (m != null) {
            contactMessageRepository.delete(m);
            return true;
        }
        return false;
    }

    public static String normalize(String input) {
        if (input == null) {
            return "";
        }
        return Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replace("đ", "d")
                .replace("Đ", "d")
                .toLowerCase(Locale.ROOT);
    }

    public static Map<String, String> labelMap() {
        Map<String, String> map = new HashMap<>();
        map.put(LOAI_DON_HANG, "Đơn hàng");
        map.put(LOAI_SAN_PHAM, "Sản phẩm");
        map.put(LOAI_GIAO_HANG, "Giao hàng");
        map.put(LOAI_THANH_TOAN, "Thanh toán");
        map.put(LOAI_KHIEU_NAI, "Khiếu nại");
        map.put(LOAI_HOP_TAC, "Hợp tác");
        map.put(LOAI_KHAC, "Khác");
        map.put(LOAI_RAC, "Có thể là rác");
        return map;
    }

    public record Classification(String phanLoai, boolean isSpam) {
    }
}