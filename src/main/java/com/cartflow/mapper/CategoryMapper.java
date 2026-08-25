package com.cartflow.mapper;

import com.cartflow.domain.entity.Category;
import com.cartflow.dto.response.CategoryResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryResponse toResponse(Category category);
}
