package com.duastore.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "FlashSales")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
/**
 * Entity ánh xạ dữ liệu flash sale (giảm giá chớp nhoáng).
 */
public class FlashSale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 200)
    private String tenChuongTrinh;

    @Column(length = 500)
    private String moTa;

    @Column(nullable = false)
    private LocalDateTime ngayBatDau;

    @Column(nullable = false)
    private LocalDateTime ngayKetThuc;

    @Column(nullable = false)
    private Boolean isActive = true;

    private Integer priority = 0;

    @OneToMany(mappedBy = "flashSale", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<FlashSaleItem> items = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (isActive == null) {
            isActive = true;
        }
        if (priority == null) {
            priority = 0;
        }
        if (items == null) {
            items = new ArrayList<>();
        }
    }

    public void addItem(FlashSaleItem item) {
        item.setFlashSale(this);
        if (items == null) {
            items = new ArrayList<>();
        }
        items.add(item);
    }
}