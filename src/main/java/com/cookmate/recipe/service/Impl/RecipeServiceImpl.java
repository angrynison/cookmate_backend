package com.cookmate.recipe.service.Impl;

import com.cookmate.member.domain.Member;
import com.cookmate.member.repository.MemberRepository;
import com.cookmate.pantry.domain.Pantry;
import com.cookmate.recipe.domain.Recipe;
import com.cookmate.recipe.domain.RecipeIngredient;
import com.cookmate.recipe.dto.RecipeIngredientResponseDto;
import com.cookmate.recipe.dto.RecipeRequestDto;
import com.cookmate.recipe.dto.RecipeResponseDto;
import com.cookmate.recipe.repository.RecipeRepository;
import com.cookmate.recipe.service.RecipeService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RecipeServiceImpl implements RecipeService {

    private final RecipeRepository recipeRepository;
    private final MemberRepository memberRepository;
    private final RestClient fastApiRestClient;


    // Fast API 서버 AI Agent 결과를 가져와 DB에 저장
    @Override
    public Long registerRecipe(Long memberId, String guestId, RecipeResponseDto.RecipeResponseSchema response) {

        Member member = null;
        if(memberId != null) {
            member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new RuntimeException("Member not found"));
        } else if(guestId == null) {
            throw new IllegalArgumentException("사용자 식별자가 필요합니다");
        }

        Recipe recipe = Recipe.create(
                member,
                guestId,
                response.title(),
                response.content(),
                response.source(),
                response.cost(),
                response.cookingTime(),
                response.level(),
                response.cuisine()
        );

        // ingredient 연결까지 해야됨, In - memory caching
        for (RecipeIngredientResponseDto.RecipeIngredientSchema recipeIngredientSchema : response.recipeIngredients()) {
            RecipeIngredient recipeIngredient = RecipeIngredient.create(
                    // 자식 fk 필드에 부모 객체 넘겨주기
                    recipe,
                    recipeIngredientSchema.name(),
                    recipeIngredientSchema.quantity(),
                    recipeIngredientSchema.unit()
            );

            // 부모(Recipe)필드 recipeIngredients(List) 필드에 자식 객체 recipeIngredient 를 넣음
            recipe.getRecipeIngredients().add(recipeIngredient);
        }

        recipeRepository.save(recipe);

        return recipe.getId();
    }

    // Fast API 서버에 사용자 응답 전송
    @Override
    public void requestRecipeToAi(Long memberId, RecipeRequestDto.RecipeRequestSchema requestDto) {

        if (requestDto.pantries() == null || requestDto.pantries().isEmpty()) {
            throw new IllegalArgumentException("pantries cannot be null or empty");
        }


        // Fast API로 요청 전송
        RecipeResponseDto.RecipeResponseSchema aiResponse = fastApiRestClient.post()
                .uri("/api/user/recipe/generate")
                .body(requestDto)
                .retrieve()
                .body(RecipeResponseDto.RecipeResponseSchema.class);

    }

    @Override
    public Long updateRecipe(Long memberId, Long recipeId, RecipeRequestDto.UpdateRequest updateRequest) {

        memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("Recipe not found"));

        recipe.update(
                updateRequest.title(),
                updateRequest.content(),
                updateRequest.source(),
                updateRequest.cost(),
                updateRequest.cookingTime(),
                updateRequest.level(),
                updateRequest.cuisine()
        );

        return recipe.getId();
    }

    @Override
    public void deleteRecipe(Long memberId, Long recipeId) {
        recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("이미 삭제된 레시피입니다"));

        recipeRepository.deleteById(recipeId);
    }

}
