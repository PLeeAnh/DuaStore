package com.duastore.service.client;

import com.duastore.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * Chong đặt hàng trùng (idempotency) cho checkout.
 *
 * Van de cu: nguoi dung bam "Đặt hàng" 2 lan lien tuc (hoặc mạng cham -> bam lai),
 * moi lan bam tao 1 don hang moi vi maDon sinh ngau nhien -> kha nang 2 don trung gio hang.
 *
 * Giai phap: client phat sinh 1 idempotencyKey (UUID) khi mo trang checkout va gui kem
 * moi request. Server giu map key -> ket qua (Order). Neu cung key gui lai:
 *  - dang xu ly (request trung) -> cho luot dau xong roi tra ve CHINH don hang da tao;
 *  - da xu ly xong -> tra lai don hang cu (khong tao them don, khong tru stock lai).
 *
 * Luu y: map nay nam trong bo nho (ConcurrentHashMap) nen chi hieu luc voi 1 instance.
 * Neu deploy nhieu instance, thay bang Redis/DB de dung chung.
 */
@Service
/**
 * Service chứa nghiệp vụ (business logic) xử lý thanh toán/đặt hàng (checkout), chống trùng lặp request (idempotency).
 */
public class CheckoutIdempotencyService {

    private static final Logger log = LoggerFactory.getLogger(CheckoutIdempotencyService.class);
    private static final long TTL_MILLIS = 24 * 60 * 60 * 1000L; // giu key toi da 24h

    private static final class State {
        final ReentrantLock lock = new ReentrantLock();
        Order result;
        Instant createdAt = Instant.now();
    }

    private final Map<String, State> states = new ConcurrentHashMap<>();

    /**
     * Thuc hien {@code action} mot lan duy nhat cho {@code key}.
     *
     * @return don hang ket qua (don tao moi hoac don cu khi bi gui trung)
     */
    public Order execute(String key, Supplier<Order> action) {
        if (key == null || key.isBlank()) {
            return action.get();
        }
        State state = states.computeIfAbsent(key, k -> new State());
        state.lock.lock();
        try {
            if (state.result != null) {
                log.info("Idempotency hit (key={}), tra lai don cu #{}", shortKey(key), state.result.getId());
                return state.result;
            }
            Order order = action.get();
            state.result = order;
            return order;
        } finally {
            state.lock.unlock();
        }
    }

    @Scheduled(fixedDelay = 30 * 60 * 1000L)
    public void cleanup() {
        Instant cutoff = Instant.now().minusMillis(TTL_MILLIS);
        states.entrySet().removeIf(e -> e.getValue().createdAt.isBefore(cutoff));
    }

    private String shortKey(String key) {
        return key.length() > 12 ? key.substring(0, 12) + "…" : key;
    }
}