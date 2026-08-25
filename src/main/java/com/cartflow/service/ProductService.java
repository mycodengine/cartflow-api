package com.cartflow.service;

import com.cartflow.domain.entity.Category;
import com.cartflow.domain.entity.Product;
import com.cartflow.dto.request.ProductRequest;
import com.cartflow.dto.response.PageResponse;
import com.cartflow.dto.response.ProductResponse;
import com.cartflow.exception.ResourceNotFoundException;
import com.cartflow.mapper.ProductMapper;
import com.cartflow.repository.CategoryRepository;
import com.cartflow.repository.ProductRepository;
import com.cartflow.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ReviewRepository reviewRepository;
    private final ProductMapper productMapper;

    public PageResponse<ProductResponse> findAll(Pageable pageable) {
        return toPage(productRepository.findAllByActiveTrue(pageable));
    }

    public PageResponse<ProductResponse> findByCategory(Long categoryId, Pageable pageable) {
        return toPage(productRepository.findAllByCategoryIdAndActiveTrue(categoryId, pageable));
    }

    public PageResponse<ProductResponse> search(String query, Pageable pageable) {
        return toPage(productRepository.search(query, pageable));
    }

    public ProductResponse findById(Long id) {
        Product product = productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        return enrichWithRating(product);
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + request.categoryId()));

        Product saved = productRepository.save(Product.builder()
                .category(category)
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .stock(request.stock())
                .sku(request.sku())
                .imageUrl(request.imageUrl())
                .build());
        log.info("Product '{}' created (SKU={})", saved.getName(), saved.getSku());
        return enrichWithRating(saved);
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = getOrThrow(id);
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + request.categoryId()));
        product.setCategory(category);
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setSku(request.sku());
        product.setImageUrl(request.imageUrl());
        log.info("Product {} updated", id);
        return enrichWithRating(productRepository.save(product));
    }

    @Transactional
    public void deactivate(Long id) {
        Product product = getOrThrow(id);
        product.setActive(false);
        productRepository.save(product);
        log.info("Product {} deactivated", id);
    }

    /** Attaches the computed average rating to the response. */
    private ProductResponse enrichWithRating(Product product) {
        ProductResponse base = productMapper.toResponse(product);
        Double avg = reviewRepository.findAverageRatingByProductId(product.getId());
        return new ProductResponse(base.id(), base.categoryId(), base.categoryName(), base.name(),
                base.description(), base.price(), base.stock(), base.sku(), base.imageUrl(),
                base.active(), avg, base.createdAt());
    }

    private Product getOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
    }

    private PageResponse<ProductResponse> toPage(Page<Product> page) {
        return new PageResponse<>(
                page.getContent().stream().map(this::enrichWithRating).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(),
                page.getTotalPages(), page.isLast());
    }
}
