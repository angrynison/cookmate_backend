package com.cookmate.recipe.domain;

import com.cookmate.ingredient.domain.Ingredient;
import com.cookmate.global.type.Unit;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "recipe_ingredient")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RecipeIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    String name;

    double quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Unit unit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredientId;
}
