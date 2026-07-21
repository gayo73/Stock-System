package com.shinhan.stock.common.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "account")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 20)
    private String accountNumber;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal cashBalance = BigDecimal.ZERO;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal withdrawableCash = BigDecimal.ZERO;

    // OMS가 매수 주문 접수 시 즉시 낙관적 락으로 withdrawableCash를 가차감함
    // 동시에 두 요청이 들어와도 버전 충돌로하나는 실패시켜 잔고가 이중 차감되지 않도록 함
    @Version
    private Long version;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // 매수 주문 접수 시: 인출가능금액만 선차감 (실제 cashBalance는 그대로 유지)
    public void reserveForBuyOrder(BigDecimal amount) {
        if (this.withdrawableCash.compareTo(amount) < 0) {
            throw new InsufficientBalanceException();
        }
        this.withdrawableCash = this.withdrawableCash.subtract(amount);
    }

    // 매수 주문 취소/거부 시: 가차감했던 금액 원복
    public void releaseBuyReservation(BigDecimal amount) {
        this.withdrawableCash = this.withdrawableCash.add(amount);
    }

    // 체결 확정 시(정산): 실제 cashBalance 확정 차감/증가
    public void confirmSettlement(BigDecimal amount) {
        this.cashBalance = this.cashBalance.subtract(amount);
    }

    public void depositFromSell(BigDecimal amount) {
        this.cashBalance = this.cashBalance.add(amount);
        this.withdrawableCash = this.withdrawableCash.add(amount);
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // 원장 모듈이 잔고를 갱신할 때마다 updatedAt이 자동 기록되어야 추적 가능
    // 장애복구/보상트랜잭션 시 이 시각 기준으로 재처리 여부 판단
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}