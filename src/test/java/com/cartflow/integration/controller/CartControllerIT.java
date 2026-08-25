package com.cartflow.integration.controller;

import com.cartflow.AbstractIntegrationTest;
import com.cartflow.domain.entity.Category;
import com.cartflow.domain.entity.Product;
import com.cartflow.dto.request.CartItemRequest;
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

class CartControllerIT extends AbstractIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired ProductRepository productRepository;
    @Autowired CategoryRepository categoryRepository;

    private String token;
    private Long productId;

    @BeforeEach
    void setup() throws Exception {
        token = register("cart_user_" + System.nanoTime() + "@example.com");

        Category category = categoryRepository.findAll().stream().findFirst()
                .orElseGet(() -> categoryRepository.save(
                        Category.builder().name("CartCat").slug("cartcat-" + System.nanoTime()).build()));

        Product p = productRepository.save(Product.builder()
                .category(category).name("Mouse").description("Wireless Mouse")
                .price(new BigDecimal("29.99")).stock(200).sku("SKU-MS-" + System.nanoTime()).active(true).build());
        productId = p.getId();
    }

    @Test
    void getCart_initiallyEmpty() throws Exception {
        mockMvc.perform(get("/api/v1/cart").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(0))
                .andExpect(jsonPath("$.items").isEmpty());
    }

    @Test
    void addItem_appearsInCart() throws Exception {
        CartItemRequest req = new CartItemRequest(productId, 3);
        mockMvc.perform(post("/api/v1/cart/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalItems").value(3));
    }

    @Test
    void removeItem_cartBecomesEmpty() throws Exception {
        CartItemRequest req = new CartItemRequest(productId, 1);
        mockMvc.perform(post("/api/v1/cart/items")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))).andReturn();

        mockMvc.perform(delete("/api/v1/cart/items/" + productId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/cart").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.totalItems").value(0));
    }

    @Test
    void clearCart_empties_everything() throws Exception {
        CartItemRequest req = new CartItemRequest(productId, 2);
        mockMvc.perform(post("/api/v1/cart/items")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))).andReturn();

        mockMvc.perform(delete("/api/v1/cart")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/cart").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.totalItems").value(0));
    }

    @Test
    void cart_requiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/cart"))
                .andExpect(status().isForbidden());
    }

    private String register(String email) throws Exception {
        RegisterRequest req = new RegisterRequest("Cart User", email, "password123");
        MvcResult r = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))).andReturn();
        return objectMapper.readTree(r.getResponse().getContentAsString()).get("accessToken").asText();
    }
}
