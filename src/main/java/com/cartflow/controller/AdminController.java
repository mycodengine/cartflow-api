package com.cartflow.controller;

import com.cartflow.dto.request.CategoryRequest;
import com.cartflow.dto.request.CouponRequest;
import com.cartflow.dto.request.OrderStatusRequest;
import com.cartflow.dto.request.ProductRequest;
import com.cartflow.dto.response.*;
import com.cartflow.service.CategoryService;
import com.cartflow.service.CouponService;
import com.cartflow.service.OrderService;
import com.cartflow.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Admin-only endpoints. Access is guarded by @PreAuthorize(ROLE_ADMIN). */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
@Tag(name = "Admin")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final OrderService orderService;
    private final CouponService couponService;

    // --- Products ---

    @PostMapping("/products")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new product")
    public ProductResponse createProduct(@Valid @RequestBody ProductRequest request) {
        return productService.create(request);
    }

    @PutMapping("/products/{id}")
    @Operation(summary = "Update an existing product")
    public ProductResponse updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return productService.update(id, request);
    }

    @DeleteMapping("/products/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deactivate a product (soft delete)")
    public void deactivateProduct(@PathVariable Long id) {
        productService.deactivate(id);
    }

    // --- Categories ---

    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new category")
    public CategoryResponse createCategory(@Valid @RequestBody CategoryRequest request) {
        return categoryService.create(request);
    }

    @PutMapping("/categories/{id}")
    @Operation(summary = "Update an existing category")
    public CategoryResponse updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        return categoryService.update(id, request);
    }

    @DeleteMapping("/categories/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a category")
    public void deleteCategory(@PathVariable Long id) {
        categoryService.delete(id);
    }

    // --- Orders ---

    @PatchMapping("/orders/{id}/status")
    @Operation(summary = "Update order status (state machine)")
    public OrderResponse updateOrderStatus(@PathVariable Long id, @Valid @RequestBody OrderStatusRequest request) {
        return orderService.updateStatus(id, request.status());
    }

    // --- Coupons ---

    @GetMapping("/coupons")
    @Operation(summary = "List all coupons")
    public List<CouponResponse> listCoupons() {
        return couponService.findAll();
    }

    @PostMapping("/coupons")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a coupon")
    public CouponResponse createCoupon(@Valid @RequestBody CouponRequest request) {
        return couponService.create(request);
    }
}
