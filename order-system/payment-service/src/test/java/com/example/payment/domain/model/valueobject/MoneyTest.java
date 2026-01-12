package com.example.payment.domain.model.valueobject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Payment Money 值物件測試")
class MoneyTest {

    @Test
    @DisplayName("建立有效的 Money")
    void of_shouldCreateValidMoney() {
        Money money = Money.of(new BigDecimal("35900"), "TWD");

        assertEquals(new BigDecimal("35900"), money.amount());
        assertEquals("TWD", money.currency());
    }

    @Test
    @DisplayName("從 long 建立 Money")
    void of_shouldCreateMoneyFromLong() {
        Money money = Money.of(100L, "USD");

        assertEquals(BigDecimal.valueOf(100), money.amount());
        assertEquals("USD", money.currency());
    }

    @Test
    @DisplayName("負數金額應拋出例外")
    void of_shouldThrowExceptionForNegativeAmount() {
        assertThrows(IllegalArgumentException.class,
                () -> Money.of(new BigDecimal("-1"), "TWD"));
    }

    @Test
    @DisplayName("無效的貨幣代碼應拋出例外")
    void of_shouldThrowExceptionForInvalidCurrency() {
        assertThrows(IllegalArgumentException.class,
                () -> Money.of(100L, "TWDD"));
        assertThrows(IllegalArgumentException.class,
                () -> Money.of(100L, "tw"));
    }
}
