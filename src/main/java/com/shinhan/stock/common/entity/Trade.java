package com.shinhan.stock.common.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "trade")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 매칭엔진이 발행한 trade.executed 이벤트를 원장이 그대로 저장하는 구조이며,
    // Order 엔티티(OMS 소유)에 JPA 연관관계로 의존하면 모듈 간 결합이 생김.
    @Column(name = "buy_order_id", nullable = false)
    private Long buyOrderId;

    @Column(name = "sell_order_id", nullable = false)
    private Long sellOrderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Long quantity;

    @Column(nullable = false)
    private LocalDateTime executedAt;

    // executedAt은 "매칭이 일어난 시각"으로, 매칭엔진에서 이벤트 생성 시점의
    // 값을 그대로 전달받아 세팅하므로 별도 @PrePersist 불필요
}