package com.cookmate.ingredient.domain;

import com.cookmate.global.type.IngredientCategory;
import com.cookmate.pantry.domain.Pantry;
import com.cookmate.recipe.domain.RecipeIngredient;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ingredient")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    String name;

    Integer defaultExpiry;
    Integer frozenExpiry;
    Integer ambientExpiry;
    Integer refrigeratedExpiry;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IngredientCategory ingredientCategory;

    @OneToMany(mappedBy = "Pantry")
    private List<Pantry> pantryId = new ArrayList<Pantry>();

    @OneToMany(mappedBy = "RecipeIngredient")
    List<RecipeIngredient> recipeIngredients = new ArrayList<RecipeIngredient>();



    // 기본재료 등록
    public static Ingredient create(
            String name,
            Integer defaultExpiry,
            Integer frozenExpiry,
            Integer ambientExpiry,
            Integer refrigeratedExpiry,
            IngredientCategory ingredientCategory
    ) {
        return Ingredient.builder()
                .name(name)
                .defaultExpiry(defaultExpiry)
                .frozenExpiry(frozenExpiry)
                .ambientExpiry(ambientExpiry)
                .refrigeratedExpiry(refrigeratedExpiry)
                .ingredientCategory(ingredientCategory)
                .build();
    }

    // 기본재료 업데이트
    public Ingredient update(
            String name,
            Integer defaultExpiry,
            Integer frozenExpiry,
            Integer ambientExpiry,
            Integer refrigeratedExpiry,
            IngredientCategory ingredientCategory
    ) {
        if (name != null) this.name = name;
        if (defaultExpiry != null) this.defaultExpiry = defaultExpiry;
        if (frozenExpiry!= null) this.frozenExpiry = frozenExpiry;
        if (ambientExpiry != null) this.ambientExpiry = ambientExpiry;
        if (refrigeratedExpiry != null) this.refrigeratedExpiry = refrigeratedExpiry;
        if (ingredientCategory != null) this.ingredientCategory = ingredientCategory;
        return this;
    }

}
