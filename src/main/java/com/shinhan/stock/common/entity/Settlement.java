package com.shinhan.stock.common.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "settlement")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trade_id", nullable = false)
    private Long tradeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private OrderSide side;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal fee;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal tax = BigDecimal.ZERO;

    @Column(nullable = false)
    private LocalDate tradeDate;

    // T+2 결제일 — 5주차 야간배치가 이 날짜 도달 여부를 조건으로 SETTLED 전환
    @Column(nullable = false)
    private LocalDate settleDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SettlementStatus status;

    // 배치가 같은 건을 중복 처리해도 상태 검증으로 이중 정산을 방지
    public void settle() {
        if (this.status != SettlementStatus.PENDING) {
            throw new IllegalStateException("이미 정산 처리됨: " + this.status);
        }
        this.status = SettlementStatus.SETTLED;
    }
}