package com.duastore.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "LoyaltyBalances")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
/**
 * So du diem tich luy HIEN TAI cua 1 khach hang — cot dem co the cap nhat NGUYEN TU
 * (UPDATE ... WHERE), tach rieng khoi so ghi LoyaltyTransactions (lich su/audit log,
 * append-only, khong co 1 dong duy nhat de dieu kien UPDATE atomic vao). Neu chi dua
 * vao LoyaltyTransactions (doc dong cuoi de suy ra "so du hien tai" roi moi ghi dong
 * moi), 2 request redeem gan nhu cung luc co the cung doc thay du diem va cung thanh
 * cong, khien khach doi duoc nhieu diem hon so thuc co (y het lop bug da sua o cac
 * cho khac trong du an — xem OrderRepository.markPaidIfUnpaid, FlashSaleItemRepository
 * .claimQuotaIfAvailable...).
 */
public class LoyaltyBalance {

    @Id
    @EqualsAndHashCode.Include
    private Integer userId;

    @Column(nullable = false)
    private Integer balance = 0;
}
