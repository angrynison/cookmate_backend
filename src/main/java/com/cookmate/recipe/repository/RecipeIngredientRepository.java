package com.cookmate.recipe.repository;

import com.cookmate.recipe.domain.RecipeIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeIngredientRepository extends JpaRepository<RecipeIngredient, Long> {

    RecipeIngredient findByName(String name);

}
