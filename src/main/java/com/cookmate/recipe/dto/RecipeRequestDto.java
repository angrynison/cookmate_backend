package com.cookmate.recipe.dto;

import com.cookmate.global.type.Cuisine;
import com.cookmate.recipe.domain.Recipe;
import lombok.Builder;

import java.util.List;
import java.util.Optional;

public class RecipeRequestDto {

    @Builder
    public record RecipeRequestSchema(
            Long memberId,
            List<String> pantries,
            Optional<List<Cuisine>> cuisines
    ) {
    }

    @Builder
    public record UpdateRequest(
            String title,
            String content,
            String source,
            Integer cost,
            String cookingTime,
            Recipe.Level level,
            Cuisine cuisine
    ){
    }


}
