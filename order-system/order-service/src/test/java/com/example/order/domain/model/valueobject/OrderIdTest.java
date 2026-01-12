package com.example.order.domain.model.valueobject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OrderId 值物件測試")
class OrderIdTest {

    @Test
    @DisplayName("生成的 OrderId 應以 ORD- 開頭")
    void generate_shouldCreateOrderIdWithCorrectPrefix() {
        OrderId orderId = OrderId.generate();

        assertNotNull(orderId);
        assertTrue(orderId.value().startsWith("ORD-"));
    }

    @Test
    @DisplayName("生成的 OrderId 長度應為 12 字元")
    void generate_shouldCreateOrderIdWithCorrectLength() {
        OrderId orderId = OrderId.generate();

        assertEquals(12, orderId.value().length());
    }

    @Test
    @DisplayName("從字串建立 OrderId 應成功")
    void of_shouldCreateOrderIdFromString() {
        String value = "ORD-12345678";
        OrderId orderId = OrderId.of(value);

        assertEquals(value, orderId.value());
    }

    @Test
    @DisplayName("空值應拋出例外")
    void of_shouldThrowExceptionForNullValue() {
        assertThrows(IllegalArgumentException.class, () -> OrderId.of(null));
    }

    @Test
    @DisplayName("空白字串應拋出例外")
    void of_shouldThrowExceptionForBlankValue() {
        assertThrows(IllegalArgumentException.class, () -> OrderId.of(""));
        assertThrows(IllegalArgumentException.class, () -> OrderId.of("   "));
    }

    @Test
    @DisplayName("多次生成的 OrderId 應不相同")
    void generate_shouldCreateUniqueIds() {
        OrderId id1 = OrderId.generate();
        OrderId id2 = OrderId.generate();

        assertNotEquals(id1.value(), id2.value());
    }
}
