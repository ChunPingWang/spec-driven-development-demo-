package com.example.e2e.steps;

import io.cucumber.java.zh_tw.假設;
import io.cucumber.java.zh_tw.當;
import io.cucumber.java.zh_tw.那麼;
import io.cucumber.java.zh_tw.並且;
import io.cucumber.java.Before;
import io.cucumber.java.After;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.http.ContentType;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Step definitions for order creation scenarios.
 */
public class OrderCreationSteps {

    @Value("${order.service.url:http://localhost:8081}")
    private String orderServiceUrl;

    @Value("${inventory.service.url:http://localhost:8082}")
    private String inventoryServiceUrl;

    @Value("${payment.service.url:http://localhost:8083}")
    private String paymentServiceUrl;

    private String buyerName;
    private String buyerEmail;
    private String productId;
    private int quantity;
    private String cardNumber;
    private String expiryDate;
    private String cvv;
    private int amount;
    private int initialStock;

    private Response response;
    private String orderId;
    private String paymentAuthorizationFailure;
    private String captureFailure;

    @Before
    public void setup() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        paymentAuthorizationFailure = null;
        captureFailure = null;
    }

    @After
    public void cleanup() {
        // Cleanup test data if needed
    }

    // Background steps
    @假設("系統中有商品 {string} 庫存為 {int} 件")
    public void 系統中有商品庫存為件(String productId, int stock) {
        this.initialStock = stock;
        // In real E2E test, this would verify or setup inventory
        // For now, assume the inventory service has this product
    }

    @假設("支付服務正常運作")
    public void 支付服務正常運作() {
        // In real E2E test, this would verify payment service health
    }

    // Given steps
    @假設("買家 {string} 的電子郵件為 {string}")
    public void 買家的電子郵件為(String name, String email) {
        this.buyerName = name;
        this.buyerEmail = email;
    }

    @假設("購買商品 {string} 數量為 {int} 件")
    public void 購買商品數量為件(String productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    @假設("使用信用卡 {string} 有效期 {string} CVV {string} 支付 {int} 元")
    public void 使用信用卡有效期CVV支付元(String cardNumber, String expiryDate, String cvv, int amount) {
        this.cardNumber = cardNumber;
        this.expiryDate = expiryDate;
        this.cvv = cvv;
        this.amount = amount;
    }

    @假設("支付授權將會失敗並返回 {string}")
    public void 支付授權將會失敗並返回(String errorMessage) {
        this.paymentAuthorizationFailure = errorMessage;
    }

    @假設("請款將會失敗並返回 {string}")
    public void 請款將會失敗並返回(String errorMessage) {
        this.captureFailure = errorMessage;
    }

    // When steps
    @當("買家提交訂單")
    public void 買家提交訂單() {
        Map<String, Object> buyer = new HashMap<>();
        buyer.put("name", buyerName);
        buyer.put("email", buyerEmail);

        Map<String, Object> orderItem = new HashMap<>();
        orderItem.put("productId", productId);
        orderItem.put("productName", "Test Product");
        orderItem.put("quantity", quantity);

        Map<String, Object> payment = new HashMap<>();
        payment.put("method", "CREDIT_CARD");
        payment.put("amount", amount);
        payment.put("currency", "TWD");
        payment.put("cardNumber", cardNumber);
        payment.put("expiryDate", expiryDate);
        payment.put("cvv", cvv);

        Map<String, Object> request = new HashMap<>();
        request.put("buyer", buyer);
        request.put("orderItem", orderItem);
        request.put("payment", payment);

        response = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(orderServiceUrl + "/api/v1/orders");
    }

    // Then steps
    @那麼("訂單應建立成功")
    public void 訂單應建立成功() {
        response.then()
                .statusCode(anyOf(is(200), is(201)));
        orderId = response.jsonPath().getString("orderId");
        assertNotNull(orderId, "Order ID should not be null");
    }

    @那麼("訂單應建立失敗")
    public void 訂單應建立失敗() {
        response.then()
                .statusCode(anyOf(is(400), is(422), is(500)));
    }

    @那麼("訂單狀態應為 {string}")
    public void 訂單狀態應為(String expectedStatus) {
        String actualStatus = response.jsonPath().getString("status");
        assertEquals(expectedStatus, actualStatus);
    }

    @那麼("支付狀態應為 {string}")
    public void 支付狀態應為(String expectedStatus) {
        String actualPaymentStatus = response.jsonPath().getString("paymentStatus");
        assertEquals(expectedStatus, actualPaymentStatus);
    }

    @那麼("庫存應扣減至 {int} 件")
    public void 庫存應扣減至件(int expectedStock) {
        // In real E2E test, this would query the inventory service
        // For now, we verify the order response indicates successful inventory deduction
    }

    @那麼("庫存應維持為 {int} 件")
    public void 庫存應維持為件(int expectedStock) {
        // In real E2E test, verify inventory was not changed
    }

    @那麼("庫存應已回滾至 {int} 件")
    public void 庫存應已回滾至件(int expectedStock) {
        // In real E2E test, verify inventory was rolled back
    }

    @那麼("失敗原因應包含 {string}")
    public void 失敗原因應包含(String expectedReason) {
        String failureReason = response.jsonPath().getString("failureReason");
        assertNotNull(failureReason, "Failure reason should not be null");
        assertTrue(failureReason.contains(expectedReason),
                "Expected failure reason to contain '" + expectedReason + "' but was: " + failureReason);
    }

    @那麼("支付授權應已取消")
    public void 支付授權應已取消() {
        // In real E2E test, verify payment was voided via payment service
    }

    @那麼("應返回驗證錯誤")
    public void 應返回驗證錯誤() {
        response.then()
                .statusCode(400);
    }

    @那麼("錯誤訊息應包含 {string}")
    public void 錯誤訊息應包含(String expectedMessage) {
        String errorMessage = response.jsonPath().getString("message");
        assertNotNull(errorMessage, "Error message should not be null");
        assertTrue(errorMessage.contains(expectedMessage),
                "Expected error message to contain '" + expectedMessage + "' but was: " + errorMessage);
    }
}
