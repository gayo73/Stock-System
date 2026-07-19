package com.shinhan.stock.common.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "stock", uniqueConstraints = @UniqueConstraint(columnNames = "code"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal tickSize;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal prevClosePrice;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal upperLimitPrice;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal lowerLimitPrice;

    // 매칭엔진이 호가를 Redis에 올리기 전
    // 가격제한폭 검증을 이 메서드로 위임 (검증 로직이 여러 곳에 흩어지지 않음)
    public boolean isPriceValid(BigDecimal price) {
        return price.compareTo(lowerLimitPrice) >= 0 && price.compareTo(upperLimitPrice) <= 0;
    }
}