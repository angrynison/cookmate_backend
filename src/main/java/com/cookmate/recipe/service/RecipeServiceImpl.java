package com.cookmate.recipe.service;

import com.cookmate.recipe.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecipeServiceImpl {

    private final RecipeRepository recipeRepository;




}
