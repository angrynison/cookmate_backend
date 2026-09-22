package com.cookmate.recipe.service;

import com.cookmate.recipe.dto.RecipeRequestDto;
import com.cookmate.recipe.dto.RecipeResponseDto;

public interface RecipeService {

    public Long registerRecipe(Long memberId, String guestId, RecipeResponseDto.RecipeResponseSchema recipeCreateFromAiDto);
    public void requestRecipeToAi(Long memberId, RecipeRequestDto.RecipeRequestSchema requestDto);
    public Long updateRecipe(Long memberId, Long recipeId, RecipeRequestDto.UpdateRequest updateRequest);
}
