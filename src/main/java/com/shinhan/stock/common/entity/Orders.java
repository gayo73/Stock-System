package com.shinhan.stock.common.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private OrderSide side;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private OrderType type;

    // MARKET 주문은 가격 미지정이므로 nullable 유지
    @Column(precision = 18, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Long quantity;

    @Column(nullable = false)
    private Long remainingQuantity;

    // (RECEIVED → PARTIALLY_FILLED → FILLED / CANCELLED / REJECTED)를
    // 그대로 아래 메서드들의 분기 조건으로 코드화
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    // 매칭엔진이 원장을 갱신하며 이 주문 상태도 함께 변경하는데, 동시에 REST로 취소 요청이 들어올 수 있음
    // 버전 충돌로 "이미 체결된 주문을 취소" 같은 레이스 컨디션을 차단
    @Version
    private Long version;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // 부분/전량 체결 처리 - RECEIVED 또는 PARTIALLY_FILLED 상태에서만 허용
    public void partiallyFill(long filledQty) {
        validateTransition(OrderStatus.RECEIVED, OrderStatus.PARTIALLY_FILLED);
        this.remainingQuantity -= filledQty;
        this.status = (this.remainingQuantity == 0)
                ? OrderStatus.FILLED
                : OrderStatus.PARTIALLY_FILLED;
    }

    // FILLED/CANCELLED 상태에서는 취소 불가
    public void cancel() {
        if (this.status == OrderStatus.FILLED || this.status == OrderStatus.CANCELLED) {
            throw new IllegalOrderStateException(this.status);
        }
        this.status = OrderStatus.CANCELLED;
    }

    public void reject() {
        this.status = OrderStatus.REJECTED;
    }

    private void validateTransition(OrderStatus... allowedFrom) {
        boolean allowed = Arrays.stream(allowedFrom).anyMatch(s -> s == this.status);
        if (!allowed) {
            throw new IllegalOrderStateException(this.status);
        }
    }

    // 이 프로젝트에서 특수한 이유: remainingQuantity는 "잔여수량"이라는
    // 이 도메인만의 불변식(최초엔 반드시 quantity와 같아야 함)이 있음.
    // 서비스 코드에서 매번 초기화하면 실수로 누락될 수 있으므로 엔티티가 강제.
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.remainingQuantity = this.quantity;
        this.status = OrderStatus.RECEIVED;
    }

    // 이 프로젝트에서 특수한 이유: 원장 모듈이 체결/취소로 주문 상태를 바꿀 때마다
    // updatedAt이 갱신되어야, 6주차 Kafka DLQ 재처리 시 "마지막 갱신 시각 이후
    // 재처리 필요 여부"를 판단하는 기준이 됨
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
