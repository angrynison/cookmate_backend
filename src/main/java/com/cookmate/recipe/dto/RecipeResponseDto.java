package com.cookmate.recipe.dto;

import com.cookmate.recipe.domain.Recipe;
import lombok.Builder;

public class RecipeResponseDto {

    @Builder
    public record RecipeCreateFromAiDto(
            String title,
            String content,
            String source,
            Integer cost,
            String cookingTime,
            Recipe.Level level
    ) {
    }

}
