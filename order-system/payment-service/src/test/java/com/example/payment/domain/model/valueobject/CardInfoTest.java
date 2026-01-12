package com.example.payment.domain.model.valueobject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CardInfo 值物件測試")
class CardInfoTest {

    @Test
    @DisplayName("從卡號建立 CardInfo 應遮蔽卡號")
    void fromCardNumber_shouldMaskCardNumber() {
        CardInfo cardInfo = CardInfo.fromCardNumber("4111111111111111", "12/26");

        assertEquals("1111", cardInfo.lastFour());
        assertEquals("12/26", cardInfo.expiryDate());
    }

    @Test
    @DisplayName("使用工廠方法建立 CardInfo")
    void of_shouldCreateCardInfo() {
        CardInfo cardInfo = CardInfo.of("1111", "12/26");

        assertEquals("1111", cardInfo.lastFour());
        assertEquals("12/26", cardInfo.expiryDate());
    }

    @Test
    @DisplayName("無效的後四碼應拋出例外")
    void of_shouldThrowExceptionForInvalidLastFour() {
        assertThrows(IllegalArgumentException.class,
                () -> CardInfo.of("111", "12/26")); // 太短
        assertThrows(IllegalArgumentException.class,
                () -> CardInfo.of("11111", "12/26")); // 太長
        assertThrows(IllegalArgumentException.class,
                () -> CardInfo.of("abcd", "12/26")); // 非數字
    }

    @Test
    @DisplayName("無效的到期日應拋出例外")
    void of_shouldThrowExceptionForInvalidExpiryDate() {
        assertThrows(IllegalArgumentException.class,
                () -> CardInfo.of("1111", "13/26")); // invalid month
        assertThrows(IllegalArgumentException.class,
                () -> CardInfo.of("1111", "1226")); // 無斜線
        assertThrows(IllegalArgumentException.class,
                () -> CardInfo.of("1111", "12/2026")); // 年份太長
    }

    @Test
    @DisplayName("getMaskedCard 應返回遮蔽卡號")
    void getMaskedCard_shouldReturnMaskedCardNumber() {
        CardInfo cardInfo = CardInfo.of("1111", "12/26");

        assertEquals("**** **** **** 1111", cardInfo.getMaskedCard());
    }
}
