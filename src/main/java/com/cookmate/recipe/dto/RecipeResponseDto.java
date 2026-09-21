package com.cookmate.recipe.dto;

import com.cookmate.global.type.Cuisine;
import com.cookmate.recipe.domain.Recipe;
import lombok.Builder;

import java.util.List;

public class RecipeResponseDto {

    @Builder
    public record RecipeCreatedFromAiDto(
            String title,
            String content,
            String source,
            Integer cost,
            String cookingTime,
            Recipe.Level level,
            Cuisine cuisine,
            List<RecipeIngredientResponseDto.RecipeIngredientSchema> recipeIngredients
    ) {
    }

}
