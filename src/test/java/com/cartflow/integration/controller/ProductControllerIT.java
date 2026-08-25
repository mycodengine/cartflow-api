package com.cartflow.integration.controller;

import com.cartflow.AbstractIntegrationTest;
import com.cartflow.domain.entity.Category;
import com.cartflow.domain.entity.Product;
import com.cartflow.dto.request.RegisterRequest;
import com.cartflow.repository.CategoryRepository;
import com.cartflow.repository.ProductRepository;
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

class ProductControllerIT extends AbstractIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired ProductRepository productRepository;
    @Autowired CategoryRepository categoryRepository;

    private Category category;

    @BeforeEach
    void seedData() {
        productRepository.deleteAll();
        category = categoryRepository.findAll().stream().findFirst()
                .orElseGet(() -> categoryRepository.save(
                        Category.builder().name("Test Cat").slug("test-cat").build()));
        productRepository.save(Product.builder()
                .category(category).name("Laptop").description("Gaming laptop")
                .price(new BigDecimal("999.99")).stock(50).sku("SKU-LAP-001").active(true).build());
    }

    @Test
    void listProducts_returnsPagedResults() throws Exception {
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("Laptop"));
    }

    @Test
    void searchProducts_byName_returnsMatches() throws Exception {
        mockMvc.perform(get("/api/v1/products/search").param("q", "Laptop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void searchProducts_noMatch_returnsEmpty() throws Exception {
        mockMvc.perform(get("/api/v1/products/search").param("q", "xyznotexist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void getProduct_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/products/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminCreate_withoutToken_returns403() throws Exception {
        String body = """
                {"categoryId":1,"name":"Phone","price":500,"stock":10,"sku":"SKU-PH1"}""";
        mockMvc.perform(post("/api/v1/admin/products")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
    }

    private String getToken(String email) throws Exception {
        RegisterRequest req = new RegisterRequest("User", email, "password123");
        MvcResult r = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))).andReturn();
        return objectMapper.readTree(r.getResponse().getContentAsString()).get("accessToken").asText();
    }
}
