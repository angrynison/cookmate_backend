package com.cookmate.recipe.dto;

import com.cookmate.pantry.domain.Pantry;
import com.cookmate.recipe.domain.Recipe;
import lombok.Builder;

public class RecipeRequestDto {

    @Builder
    public record RecipeCreateToAiDto(
            Long user_id,
            Pantry


    ) {
    }

    @Builder
    public record RecipeUpdateDto(
            String title,
            String content,
            String source,
            Integer cost,
            String cookingTime,
            Recipe.Level level
    ){
    }


}
