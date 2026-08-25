package com.cartflow.service;

import com.cartflow.domain.entity.Coupon;
import com.cartflow.domain.enums.DiscountType;
import com.cartflow.dto.request.CouponRequest;
import com.cartflow.dto.response.CouponResponse;
import com.cartflow.exception.BusinessException;
import com.cartflow.exception.ResourceNotFoundException;
import com.cartflow.mapper.CouponMapper;
import com.cartflow.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponService {

    private final CouponRepository couponRepository;
    private final CouponMapper couponMapper;

    public List<CouponResponse> findAll() {
        return couponRepository.findAll().stream().map(couponMapper::toResponse).toList();
    }

    @Transactional
    public CouponResponse create(CouponRequest request) {
        Coupon coupon = Coupon.builder()
                .code(request.code().toUpperCase())
                .description(request.description())
                .discountType(request.discountType())
                .discountValue(request.discountValue())
                .minOrderValue(request.minOrderValue() != null ? request.minOrderValue() : BigDecimal.ZERO)
                .maxUses(request.maxUses())
                .expiresAt(request.expiresAt())
                .build();
        Coupon saved = couponRepository.save(coupon);
        log.info("Coupon '{}' created", saved.getCode());
        return couponMapper.toResponse(saved);
    }

    /**
     * Validates and applies a coupon to the given order subtotal.
     * Increments the uses counter and returns the discount amount.
     */
    @Transactional
    public BigDecimal apply(String code, BigDecimal subtotal) {
        Coupon coupon = couponRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new BusinessException("Coupon not found: " + code));

        if (!coupon.isUsable()) {
            throw new BusinessException("Coupon is no longer valid: " + code);
        }
        if (subtotal.compareTo(coupon.getMinOrderValue()) < 0) {
            throw new BusinessException("Minimum order value for this coupon is " + coupon.getMinOrderValue());
        }

        BigDecimal discount = coupon.getDiscountType() == DiscountType.PERCENTAGE
                ? subtotal.multiply(coupon.getDiscountValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                : coupon.getDiscountValue().min(subtotal); // never give more discount than order value

        coupon.setUsesCount(coupon.getUsesCount() + 1);
        couponRepository.save(coupon);
        log.info("Coupon '{}' applied — discount={}", code, discount);
        return discount;
    }
}
