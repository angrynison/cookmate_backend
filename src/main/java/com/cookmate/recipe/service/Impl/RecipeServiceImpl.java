package com.cookmate.recipe.service.Impl;

import com.cookmate.recipe.domain.Recipe;
import com.cookmate.recipe.repository.RecipeRepository;
import com.cookmate.recipe.service.RecipeService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class RecipeServiceImpl implements RecipeService {

    private final RecipeRepository recipeRepository;


    public Recipe




}
