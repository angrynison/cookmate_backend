package com.cookmate.recipe.service.Impl;

import com.cookmate.RestClientConfig;
import com.cookmate.recipe.domain.Recipe;
import com.cookmate.recipe.domain.RecipeIngredient;
import com.cookmate.recipe.dto.RecipeIngredientResponseDto;
import com.cookmate.recipe.dto.RecipeRequestDto;
import com.cookmate.recipe.dto.RecipeResponseDto;
import com.cookmate.recipe.repository.RecipeRepository;
import com.cookmate.recipe.service.RecipeService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
@Transactional
public class RecipeServiceImpl implements RecipeService {

    private final RecipeRepository recipeRepository;
    private final RestClient fastApiRestClient;


    // Fast API 서버 AI Agent 결과를 가져와 DB에 저장
    @Override
    public Long createRecipe(RecipeResponseDto.RecipeCreatedFromAiDto response) {

        Recipe recipe = Recipe.create(
                response.title(),
                response.content(),
                response.source(),
                response.cost(),
                response.cookingTime(),
                response.level(),
                response.cuisine()
        );

        // ingredient 연결까지 해야됨, In - memory caching
        for (RecipeIngredientResponseDto.RecipeIngredientSchema recipeIngredientFromAiDto : response.recipeIngredients()) {
            RecipeIngredient recipeIngredient = RecipeIngredient.create(
                    // 자식 fk 필드에 부모 객체 넘겨주기
                    recipe,
                    recipeIngredientFromAiDto.name(),
                    recipeIngredientFromAiDto.quantity(),
                    recipeIngredientFromAiDto.unit()
            );

            // 부모(Recipe)필드 recipeIngredients(List) 필드에 자식 객체 recipeIngredient 를 넣음
            recipe.getRecipeIngredients().add(recipeIngredient);
        }

        recipeRepository.save(recipe);

        return recipe.getId();
    }

    // Fast API 서버에 사용자 응답 전송
    @Override
    public void requestRecipeToAi(Long memberId, RecipeRequestDto.RecipeSchema requestDto) {

        if (requestDto.pantries() == null || requestDto.pantries().isEmpty() ) {
            throw new IllegalArgumentException("pantries cannot be null or empty");
        }


        // Fast API로 요청 전송
        RecipeResponseDto.RecipeCreatedFromAiDto aiResponse = fastApiRestClient.post()
                .uri("/api/recipe/generate")
                .body(requestDto)
                .retrieve()
                .body(RecipeResponseDto.RecipeCreatedFromAiDto.class);

    }








}
