package com.cookmate.recipe.service;

import com.cookmate.recipe.dto.RecipeRequestDto;
import com.cookmate.recipe.dto.RecipeResponseDto;

public interface RecipeService {

    public Long createRecipe(RecipeResponseDto.RecipeCreatedFromAiDto recipeCreateFromAiDto);
    public void requestRecipeToAi(Long memberId, RecipeRequestDto.RecipeSchema requestDto);
}
