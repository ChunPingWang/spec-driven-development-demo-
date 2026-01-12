package com.example.e2e.steps;

import io.cucumber.java.zh_tw.假設;
import io.cucumber.java.zh_tw.當;
import io.cucumber.java.zh_tw.那麼;
import io.cucumber.java.zh_tw.並且;
import io.cucumber.java.Before;
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
 * Step definitions for payment processing scenarios.
 */
public class PaymentProcessingSteps {

    @Value("${payment.service.url:http://localhost:8083}")
    private String paymentServiceUrl;

    private int amount;
    private String currency;
    private String cardNumber;
    private String expiryDate;
    private String cvv;
    private String paymentId;
    private String authorizationId;

    private Response response;

    @Before
    public void setup() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        paymentId = null;
        authorizationId = null;
    }

    // Given steps
    @假設("有效的支付請求金額為 {int} 元幣別為 {string}")
    public void 有效的支付請求金額為元幣別為(int amount, String currency) {
        this.amount = amount;
        this.currency = currency;
    }

    @假設("信用卡號為 {string} 有效期為 {string} CVV 為 {string}")
    public void 信用卡號為有效期為CVV為(String cardNumber, String expiryDate, String cvv) {
        this.cardNumber = cardNumber;
        this.expiryDate = expiryDate;
        this.cvv = cvv;
    }

    // When steps
    @當("執行支付授權")
    public void 執行支付授權() {
        Map<String, Object> request = new HashMap<>();
        request.put("orderId", "ORD-" + System.currentTimeMillis());
        request.put("amount", amount);
        request.put("currency", currency);
        request.put("cardNumber", cardNumber);
        request.put("expiryDate", expiryDate);
        request.put("cvv", cvv);

        response = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(paymentServiceUrl + "/api/v1/payments/authorize");
    }

    @當("執行請款")
    public void 執行請款() {
        assertNotNull(paymentId, "Payment ID required for capture");

        Map<String, Object> request = new HashMap<>();
        request.put("paymentId", paymentId);

        response = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(paymentServiceUrl + "/api/v1/payments/capture");
    }

    @當("再次執行請款")
    public void 再次執行請款() {
        執行請款();
    }

    @當("取消支付授權")
    public void 取消支付授權() {
        assertNotNull(paymentId, "Payment ID required for void");

        Map<String, Object> request = new HashMap<>();
        request.put("paymentId", paymentId);

        response = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(paymentServiceUrl + "/api/v1/payments/void");
    }

    // Then steps
    @那麼("授權應成功")
    public void 授權應成功() {
        response.then()
                .statusCode(anyOf(is(200), is(201)));
        paymentId = response.jsonPath().getString("paymentId");
        authorizationId = response.jsonPath().getString("authorizationId");
        assertNotNull(paymentId, "Payment ID should not be null after successful authorization");
    }

    @那麼("授權應失敗")
    public void 授權應失敗() {
        response.then()
                .statusCode(anyOf(is(400), is(422)));
    }

    @那麼("應返回支付編號")
    public void 應返回支付編號() {
        assertNotNull(paymentId, "Payment ID should be returned");
    }

    @那麼("請款應成功")
    public void 請款應成功() {
        response.then()
                .statusCode(anyOf(is(200), is(201)));
    }

    @那麼("請款應失敗")
    public void 請款應失敗() {
        response.then()
                .statusCode(anyOf(is(400), is(409), is(422)));
    }

    @那麼("取消應成功")
    public void 取消應成功() {
        response.then()
                .statusCode(anyOf(is(200), is(201)));
    }

    @那麼("失敗原因應為 {string}")
    public void 失敗原因應為(String expectedReason) {
        String actualReason = response.jsonPath().getString("message");
        if (actualReason == null) {
            actualReason = response.jsonPath().getString("failureReason");
        }
        assertNotNull(actualReason, "Failure reason should not be null");
        assertEquals(expectedReason, actualReason);
    }
}
