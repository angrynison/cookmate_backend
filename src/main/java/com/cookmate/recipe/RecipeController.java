package com.cookmate.recipe;

import com.cookmate.recipe.dto.RecipeRequestDto;
import com.cookmate.recipe.dto.RecipeResponseDto;
import com.cookmate.recipe.service.RecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

// fast api 요청은 서비스 단에서 RestClient Bean을 주입받아 진행
@Controller
@RequestMapping("/api/user/recipe")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;

    @PostMapping("/register")
    public ResponseEntity<Long> registerRecipe(
            @RequestAttribute("memberId") Long memberId,
            @RequestHeader(value = "X-Guest-Id", required = false) String guestId, // 프론트가 보내주는 UUID
            @RequestBody RecipeResponseDto.RecipeResponseSchema registerRequest) {

        return ResponseEntity.status(HttpStatus.CREATED).body(recipeService.registerRecipe(memberId, guestId, registerRequest));
    }

    @PatchMapping("/update/{recipeId}")
    public ResponseEntity<Long> updateRecipe(
            @RequestAttribute("memberId") Long memberId,
            @PathVariable Long recipeId,
            @RequestBody RecipeRequestDto.UpdateRequest updateRequest
    ) {
        return ResponseEntity.ok(recipeService.updateRecipe(memberId,recipeId,updateRequest));
    }








}
