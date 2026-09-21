package com.cookmate.recipe.dto;

import com.cookmate.global.type.Unit;
import lombok.Builder;

public class RecipeIngredientResponseDto {

    @Builder
    public record RecipeIngredientSchema(
            String name,
            Integer quantity,
            Unit unit
    ){};

}
