package com.cartflow.mapper;

import com.cartflow.domain.entity.Coupon;
import com.cartflow.dto.response.CouponResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CouponMapper {
    CouponResponse toResponse(Coupon coupon);
}
