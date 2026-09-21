package com.cookmate.recipe.dto;

import com.cookmate.global.type.Cuisine;
import lombok.Builder;

import java.util.List;
import java.util.Optional;

public class RecipeRequestDto {

    @Builder
    public record RecipeSchema(
            Long memberId,
            List<String> pantries,
            Optional<List<Cuisine>> cuisines
    ) {
    }


}
