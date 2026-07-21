package com.shinhan.stock.common.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "holding",
        uniqueConstraints = @UniqueConstraint(columnNames = {"account_id", "stock_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Holding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(nullable = false)
    private Long quantity = 0L;

    @Column(nullable = false)
    private Long sellableQuantity = 0L;

    // 동시에 여러 매도 주문이 sellableQuantity를 차감하려 할 때
    // 버전 충돌로 순차 처리되게 강제 (매도가능수량 음수 방지)
    @Version
    private Long version;

    // 매도 주문 : sellable만 선차감, quantity(총보유)는 그대로 유지
    // 체결 전까지는 실제 보유수량이 줄지 않은 것으로 취급
    public void reserveForSellOrder(long qty) {
        if (this.sellableQuantity < qty) {
            throw new InsufficientHoldingException();
        }
        this.sellableQuantity -= qty;
    }

    // 매도 체결 확정 : quantity도 실제로 차감
    public void confirmSell(long qty) {
        this.quantity -= qty;
    }

    // 매도 주문 취소/거부 : 선차감했던 sellable 원복
    public void releaseSellReservation(long qty) {
        this.sellableQuantity += qty;
    }

    // 매수 체결 확정 : 총보유/매도가능 둘 다 증가
    public void confirmBuy(long qty) {
        this.quantity += qty;
        this.sellableQuantity += qty;
    }
}