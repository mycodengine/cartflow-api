package com.cartflow.mapper;

import com.cartflow.domain.entity.Review;
import com.cartflow.dto.response.ReviewResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "userId",    source = "user.id")
    @Mapping(target = "userName",  source = "user.name")
    ReviewResponse toResponse(Review review);
}
