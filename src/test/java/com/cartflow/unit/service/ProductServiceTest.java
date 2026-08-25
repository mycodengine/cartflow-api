package com.cartflow.unit.service;

import com.cartflow.domain.entity.Category;
import com.cartflow.domain.entity.Product;
import com.cartflow.dto.request.ProductRequest;
import com.cartflow.dto.response.ProductResponse;
import com.cartflow.exception.ResourceNotFoundException;
import com.cartflow.mapper.ProductMapper;
import com.cartflow.repository.CategoryRepository;
import com.cartflow.repository.ProductRepository;
import com.cartflow.repository.ReviewRepository;
import com.cartflow.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock ProductRepository productRepository;
    @Mock CategoryRepository categoryRepository;
    @Mock ReviewRepository reviewRepository;
    @Mock ProductMapper productMapper;
    @InjectMocks ProductService productService;

    @Test
    void findById_returnsProduct_whenExists() {
        Product product = buildProduct();
        when(productRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(product));
        when(productMapper.toResponse(product)).thenReturn(buildResponse(product));
        when(reviewRepository.findAverageRatingByProductId(1L)).thenReturn(4.5);

        ProductResponse response = productService.findById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.averageRating()).isEqualTo(4.5);
    }

    @Test
    void findById_throws_whenNotFound() {
        when(productRepository.findByIdAndActiveTrue(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> productService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void create_savesProduct_andReturnsResponse() {
        Category category = Category.builder().id(1L).name("Electronics").build();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        Product saved = buildProduct();
        when(productRepository.save(any())).thenReturn(saved);
        when(productMapper.toResponse(saved)).thenReturn(buildResponse(saved));
        when(reviewRepository.findAverageRatingByProductId(1L)).thenReturn(null);

        ProductRequest req = new ProductRequest(1L, "Widget", "desc", BigDecimal.TEN, 100, "SKU-001", null);
        ProductResponse resp = productService.create(req);

        assertThat(resp.name()).isEqualTo("Widget");
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void create_throws_whenCategoryNotFound() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());
        ProductRequest req = new ProductRequest(99L, "Widget", null, BigDecimal.TEN, 10, "SKU-X", null);
        assertThatThrownBy(() -> productService.create(req))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category not found");
    }

    @Test
    void deactivate_setsActiveToFalse() {
        Product product = buildProduct();
        product.setActive(true);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.deactivate(1L);

        assertThat(product.isActive()).isFalse();
        verify(productRepository).save(product);
    }

    // --- Helpers ---
    private Product buildProduct() {
        Category cat = Category.builder().id(1L).name("Electronics").slug("electronics").build();
        return Product.builder().id(1L).name("Widget").price(BigDecimal.TEN)
                .stock(100).sku("SKU-001").category(cat).active(true).build();
    }

    private ProductResponse buildResponse(Product p) {
        return new ProductResponse(p.getId(), 1L, "Electronics", p.getName(), null,
                p.getPrice(), p.getStock(), p.getSku(), null, p.isActive(), null, null);
    }
}
