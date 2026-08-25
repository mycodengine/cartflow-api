package com.cartflow.controller;

import com.cartflow.dto.response.PageResponse;
import com.cartflow.dto.response.ProductResponse;
import com.cartflow.dto.response.ReviewResponse;
import com.cartflow.service.ProductService;
import com.cartflow.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products")
public class ProductController {

    private final ProductService productService;
    private final ReviewService reviewService;

    @GetMapping
    @Operation(summary = "List active products (public, paginated)")
    public PageResponse<ProductResponse> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort) {
        return productService.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sort)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search products by name or description (public)")
    public PageResponse<ProductResponse> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return productService.search(q, PageRequest.of(page, size));
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "List products by category (public)")
    public PageResponse<ProductResponse> findByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return productService.findByCategory(categoryId, PageRequest.of(page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a product by ID (public)")
    public ProductResponse findById(@PathVariable Long id) {
        return productService.findById(id);
    }

    @GetMapping("/{id}/reviews")
    @Operation(summary = "List reviews for a product (public)")
    public PageResponse<ReviewResponse> getReviews(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return reviewService.findByProduct(id, PageRequest.of(page, size));
    }
}
