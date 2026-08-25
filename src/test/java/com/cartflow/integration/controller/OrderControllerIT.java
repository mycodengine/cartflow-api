package com.cartflow.integration.controller;

import com.cartflow.AbstractIntegrationTest;
import com.cartflow.domain.entity.Category;
import com.cartflow.domain.entity.Product;
import com.cartflow.dto.request.CartItemRequest;
import com.cartflow.dto.request.CheckoutRequest;
import com.cartflow.dto.request.RegisterRequest;
import com.cartflow.repository.CategoryRepository;
import com.cartflow.repository.ProductRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class OrderControllerIT extends AbstractIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired ProductRepository productRepository;
    @Autowired CategoryRepository categoryRepository;

    private String userToken;
    private Long productId;

    @BeforeEach
    void setup() throws Exception {
        userToken = register("order_user_" + System.nanoTime() + "@example.com");

        Category category = categoryRepository.findAll().stream().findFirst()
                .orElseGet(() -> categoryRepository.save(
                        Category.builder().name("TestCat").slug("testcat-" + System.nanoTime()).build()));

        Product p = productRepository.save(Product.builder()
                .category(category).name("Headphones").description("Wireless")
                .price(new BigDecimal("79.99")).stock(100).sku("SKU-HP-" + System.nanoTime()).active(true).build());
        productId = p.getId();
    }

    @Test
    void checkout_createsOrder_andClearsCart() throws Exception {
        // Add item to cart
        CartItemRequest cartReq = new CartItemRequest(productId, 2);
        mockMvc.perform(post("/api/v1/cart/items")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cartReq)))
                .andExpect(status().isCreated());

        // Checkout
        CheckoutRequest checkoutReq = new CheckoutRequest("123 Main St, City", null, null);
        mockMvc.perform(post("/api/v1/orders/checkout")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkoutReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.total").isNumber());

        // Cart should be empty
        mockMvc.perform(get("/api/v1/cart")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(0));
    }

    @Test
    void checkout_withEmptyCart_returns400() throws Exception {
        CheckoutRequest req = new CheckoutRequest("123 Main St", null, null);
        mockMvc.perform(post("/api/v1/orders/checkout")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getOrders_returnsUserOrders() throws Exception {
        mockMvc.perform(get("/api/v1/orders")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void checkout_withoutAuth_returns403() throws Exception {
        CheckoutRequest req = new CheckoutRequest("123 Main St", null, null);
        mockMvc.perform(post("/api/v1/orders/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    private String register(String email) throws Exception {
        RegisterRequest req = new RegisterRequest("Test User", email, "password123");
        MvcResult r = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))).andReturn();
        JsonNode node = objectMapper.readTree(r.getResponse().getContentAsString());
        return node.get("accessToken").asText();
    }
}
