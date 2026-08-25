package com.cartflow.mapper;

import com.cartflow.domain.entity.Product;
import com.cartflow.dto.response.ProductResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "categoryId",   source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "averageRating", ignore = true) // set by service after DB query
    ProductResponse toResponse(Product product);
}
