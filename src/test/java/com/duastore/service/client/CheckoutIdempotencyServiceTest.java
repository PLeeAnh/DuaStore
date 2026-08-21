package com.duastore.service.client;

import com.duastore.model.Order;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class CheckoutIdempotencyServiceTest {

    private final CheckoutIdempotencyService service = new CheckoutIdempotencyService();

    private Order newOrder(int id) {
        Order o = new Order();
        o.setId(id);
        return o;
    }

    @Test
    void sameKey_alwaysReturnsFirstOrderAndRunsActionOnce() {
        AtomicInteger runs = new AtomicInteger();
        Order first = service.execute("ck-test-1", () -> {
            runs.incrementAndGet();
            return newOrder(42);
        });
        Order second = service.execute("ck-test-1", () -> {
            runs.incrementAndGet();
            return newOrder(99);
        });

        assertThat(runs.get()).isEqualTo(1);
        assertThat(first).isSameAs(second);
        assertThat(second.getId()).isEqualTo(42);
    }

    @Test
    void differentKeysEachRunActionOnce() {
        AtomicInteger runs = new AtomicInteger();
        Order a = service.execute("key-a", () -> {
            runs.incrementAndGet();
            return newOrder(1);
        });
        Order b = service.execute("key-b", () -> {
            runs.incrementAndGet();
            return newOrder(2);
        });

        assertThat(runs.get()).isEqualTo(2);
        assertThat(a.getId()).isEqualTo(1);
        assertThat(b.getId()).isEqualTo(2);
    }

    @Test
    void nullOrBlankKeyAlwaysRunsAction() {
        Order a = service.execute(null, () -> newOrder(1));
        Order b = service.execute(null, () -> newOrder(2));
        Order c = service.execute("   ", () -> newOrder(3));

        assertThat(a.getId()).isEqualTo(1);
        assertThat(b.getId()).isEqualTo(2);
        assertThat(c.getId()).isEqualTo(3);
    }

    @Test
    void concurrentSingleKeyExecutesActionExactlyOnce() throws Exception {
        AtomicInteger runs = new AtomicInteger();
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch go = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(2);

        Runnable task = () -> {
            ready.countDown();
            try {
                go.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            service.execute("ck-concurrent", () -> {
                runs.incrementAndGet();
                Order o = newOrder(7);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
                return o;
            });
            done.countDown();
        };

        Thread t1 = new Thread(task);
        Thread t2 = new Thread(task);
        t1.start();
        t2.start();
        assertThat(ready.await(2, TimeUnit.SECONDS)).isTrue();
        go.countDown();
        assertThat(done.await(3, TimeUnit.SECONDS)).isTrue();

        assertThat(runs.get()).isEqualTo(1);
    }

    @Test
    void cleanupRemovesExpiredEntries() throws Exception {
        Order order = service.execute("ck-expired", () -> newOrder(1));
        assertThat(order).isNotNull();

        // Bypass TTL 24h: day createdAt ve qua khu roi goi cleanup truc tiep.
        Field statesField = CheckoutIdempotencyService.class.getDeclaredField("states");
        statesField.setAccessible(true);
        Map<String, Object> states = (Map<String, Object>) statesField.get(service);
        Object state = states.get("ck-expired");
        assertThat(state).isNotNull();

        Field createdAtField = state.getClass().getDeclaredField("createdAt");
        createdAtField.setAccessible(true);
        createdAtField.set(state, Instant.now().minusSeconds(25 * 60 * 60L));

        service.cleanup();
        assertThat(states).doesNotContainKey("ck-expired");
    }
}