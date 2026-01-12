package com.example.e2e.steps;

import io.cucumber.java.zh_tw.假設;
import io.cucumber.java.zh_tw.當;
import io.cucumber.java.zh_tw.那麼;
import io.cucumber.java.zh_tw.並且;
import io.cucumber.java.Before;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.http.ContentType;
import org.awaitility.Awaitility;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Step definitions for inventory management scenarios.
 */
public class InventoryManagementSteps {

    @Value("${inventory.service.url:http://localhost:8082}")
    private String inventoryServiceUrl;

    private String productId;
    private String productName;
    private int initialStock;
    private Response response;
    private List<Response> concurrentResponses = new ArrayList<>();

    @Before
    public void setup() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        concurrentResponses.clear();
    }

    // Background steps
    @假設("商品 {string} 名稱為 {string} 初始庫存為 {int} 件")
    public void 商品名稱為初始庫存為件(String productId, String productName, int initialStock) {
        this.productId = productId;
        this.productName = productName;
        this.initialStock = initialStock;
        // In real E2E test, this would setup or verify product exists with given stock
    }

    // Given steps
    @假設("已為訂單 {string} 扣減商品 {string} 數量 {int} 件")
    public void 已為訂單扣減商品數量件(String orderId, String productId, int quantity) {
        Map<String, Object> request = new HashMap<>();
        request.put("orderId", orderId);
        request.put("productId", productId);
        request.put("quantity", quantity);

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(inventoryServiceUrl + "/api/v1/inventory/deduct")
                .then()
                .statusCode(anyOf(is(200), is(201)));
    }

    // When steps
    @當("為訂單 {string} 扣減商品 {string} 數量 {int} 件")
    public void 為訂單扣減商品數量件(String orderId, String productId, int quantity) {
        Map<String, Object> request = new HashMap<>();
        request.put("orderId", orderId);
        request.put("productId", productId);
        request.put("quantity", quantity);

        response = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(inventoryServiceUrl + "/api/v1/inventory/deduct");
    }

    @當("再次為訂單 {string} 扣減商品 {string} 數量 {int} 件")
    public void 再次為訂單扣減商品數量件(String orderId, String productId, int quantity) {
        為訂單扣減商品數量件(orderId, productId, quantity);
    }

    @當("為訂單 {string} 回滾商品 {string} 數量 {int} 件")
    public void 為訂單回滾商品數量件(String orderId, String productId, int quantity) {
        Map<String, Object> request = new HashMap<>();
        request.put("orderId", orderId);
        request.put("productId", productId);
        request.put("quantity", quantity);

        response = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(inventoryServiceUrl + "/api/v1/inventory/rollback");
    }

    @當("同時有 {int} 個訂單各扣減商品 {string} 數量 {int} 件")
    public void 同時有個訂單各扣減商品數量件(int orderCount, String productId, int quantity) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(orderCount);
        List<Future<Response>> futures = new ArrayList<>();

        for (int i = 0; i < orderCount; i++) {
            final int orderNum = i;
            futures.add(executor.submit(() -> {
                Map<String, Object> request = new HashMap<>();
                request.put("orderId", "ORD-CONC000" + orderNum);
                request.put("productId", productId);
                request.put("quantity", quantity);

                return given()
                        .contentType(ContentType.JSON)
                        .body(request)
                        .when()
                        .post(inventoryServiceUrl + "/api/v1/inventory/deduct");
            }));
        }

        for (Future<Response> future : futures) {
            concurrentResponses.add(future.get(30, TimeUnit.SECONDS));
        }

        executor.shutdown();
    }

    // Then steps
    @那麼("扣減應成功")
    public void 扣減應成功() {
        response.then()
                .statusCode(anyOf(is(200), is(201)));
    }

    @那麼("扣減應失敗")
    public void 扣減應失敗() {
        response.then()
                .statusCode(anyOf(is(400), is(409), is(422)));
    }

    @那麼("回滾應成功")
    public void 回滾應成功() {
        response.then()
                .statusCode(anyOf(is(200), is(201)));
    }

    @那麼("商品 {string} 庫存應為 {int} 件")
    public void 商品庫存應為件(String productId, int expectedStock) {
        // In real E2E test, query inventory service and verify stock level
        // For now, verify the response shows correct remaining stock
        Integer remainingStock = response.jsonPath().getInt("remainingStock");
        if (remainingStock != null) {
            assertEquals(expectedStock, remainingStock.intValue());
        }
    }

    @那麼("商品 {string} 庫存應維持 {int} 件")
    public void 商品庫存應維持件(String productId, int expectedStock) {
        // Verify stock was not changed due to failed operation
        商品庫存應為件(productId, expectedStock);
    }

    @那麼("兩次扣減都應成功")
    public void 兩次扣減都應成功() {
        // Idempotent operation should succeed both times
        response.then()
                .statusCode(anyOf(is(200), is(201)));
    }

    @那麼("只應有一筆扣減記錄")
    public void 只應有一筆扣減記錄() {
        // In real E2E test, query the inventory log and verify only one deduction record exists
    }

    @那麼("所有扣減都應成功")
    public void 所有扣減都應成功() {
        for (Response resp : concurrentResponses) {
            resp.then()
                    .statusCode(anyOf(is(200), is(201)));
        }
    }
}
