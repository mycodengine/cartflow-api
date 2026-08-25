package com.cartflow.controller;

import com.cartflow.domain.entity.User;
import com.cartflow.dto.request.CartItemRequest;
import com.cartflow.dto.response.CartResponse;
import com.cartflow.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Tag(name = "Cart")
@SecurityRequirement(name = "bearerAuth")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "View current user's cart")
    public CartResponse getCart(@AuthenticationPrincipal User user) {
        return cartService.getCart(user.getId());
    }

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add an item to cart (or increment quantity if already present)")
    public CartResponse addItem(@AuthenticationPrincipal User user,
                                @Valid @RequestBody CartItemRequest request) {
        return cartService.addItem(user.getId(), request);
    }

    @PutMapping("/items/{productId}")
    @Operation(summary = "Set exact quantity for a cart item (0 removes it)")
    public CartResponse updateItem(@AuthenticationPrincipal User user,
                                   @PathVariable Long productId,
                                   @RequestParam int quantity) {
        return cartService.updateItem(user.getId(), productId, quantity);
    }

    @DeleteMapping("/items/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove a product from the cart")
    public void removeItem(@AuthenticationPrincipal User user, @PathVariable Long productId) {
        cartService.removeItem(user.getId(), productId);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Clear the entire cart")
    public void clearCart(@AuthenticationPrincipal User user) {
        cartService.clearCart(user.getId());
    }
}
