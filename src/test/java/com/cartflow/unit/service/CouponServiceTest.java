package com.cartflow.unit.service;

import com.cartflow.domain.entity.Coupon;
import com.cartflow.domain.enums.DiscountType;
import com.cartflow.exception.BusinessException;
import com.cartflow.mapper.CouponMapper;
import com.cartflow.repository.CouponRepository;
import com.cartflow.service.CouponService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock CouponRepository couponRepository;
    @Mock CouponMapper couponMapper;
    @InjectMocks CouponService couponService;

    @Test
    void apply_calculatesPercentageDiscount() {
        Coupon coupon = buildCoupon(DiscountType.PERCENTAGE, new BigDecimal("20"), null, null, 0);
        when(couponRepository.findByCodeIgnoreCase("SAVE20")).thenReturn(Optional.of(coupon));

        BigDecimal discount = couponService.apply("SAVE20", new BigDecimal("100.00"));

        assertThat(discount).isEqualByComparingTo("20.00");
        verify(couponRepository).save(coupon);
    }

    @Test
    void apply_calculatesFixedDiscount() {
        Coupon coupon = buildCoupon(DiscountType.FIXED_AMOUNT, new BigDecimal("15"), null, null, 0);
        when(couponRepository.findByCodeIgnoreCase("FLAT15")).thenReturn(Optional.of(coupon));

        BigDecimal discount = couponService.apply("FLAT15", new BigDecimal("100.00"));

        assertThat(discount).isEqualByComparingTo("15.00");
    }

    @Test
    void apply_fixedDiscount_cannotExceedOrderTotal() {
        Coupon coupon = buildCoupon(DiscountType.FIXED_AMOUNT, new BigDecimal("200"), null, null, 0);
        when(couponRepository.findByCodeIgnoreCase("BIG")).thenReturn(Optional.of(coupon));

        BigDecimal discount = couponService.apply("BIG", new BigDecimal("50.00"));

        assertThat(discount).isEqualByComparingTo("50.00"); // capped at order value
    }

    @Test
    void apply_throwsWhenCouponExpired() {
        Coupon coupon = buildCoupon(DiscountType.PERCENTAGE, new BigDecimal("10"),
                LocalDateTime.now().minusDays(1), null, 0);
        when(couponRepository.findByCodeIgnoreCase("OLD")).thenReturn(Optional.of(coupon));

        assertThatThrownBy(() -> couponService.apply("OLD", new BigDecimal("50")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("no longer valid");
    }

    @Test
    void apply_throwsWhenMaxUsesReached() {
        Coupon coupon = buildCoupon(DiscountType.PERCENTAGE, new BigDecimal("10"), null, 5, 5);
        when(couponRepository.findByCodeIgnoreCase("USED")).thenReturn(Optional.of(coupon));

        assertThatThrownBy(() -> couponService.apply("USED", new BigDecimal("50")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("no longer valid");
    }

    @Test
    void apply_throwsWhenBelowMinOrderValue() {
        Coupon coupon = buildCoupon(DiscountType.PERCENTAGE, new BigDecimal("10"), null, null, 0);
        coupon.setMinOrderValue(new BigDecimal("100"));
        when(couponRepository.findByCodeIgnoreCase("MIN100")).thenReturn(Optional.of(coupon));

        assertThatThrownBy(() -> couponService.apply("MIN100", new BigDecimal("50")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Minimum order value");
    }

    private Coupon buildCoupon(DiscountType type, BigDecimal value, LocalDateTime expiresAt,
                                Integer maxUses, int usesCount) {
        return Coupon.builder().code("TEST").discountType(type).discountValue(value)
                .minOrderValue(BigDecimal.ZERO).maxUses(maxUses).usesCount(usesCount)
                .active(true).expiresAt(expiresAt).build();
    }
}
