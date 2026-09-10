package com.cookmate.cookmate.pantry;

import com.cookmate.global.security.JwtProvider;
import com.cookmate.global.type.Cuisine;
import com.cookmate.global.type.IngredientCategory;
import com.cookmate.global.type.StorageType;
import com.cookmate.global.type.Unit;
import com.cookmate.ingredient.domain.Ingredient;
import com.cookmate.ingredient.dto.IngredientRequestDto;
import com.cookmate.ingredient.repository.IngredientRepository;
import com.cookmate.member.domain.Member;
import com.cookmate.member.dto.MemberRequestDto;
import com.cookmate.member.dto.MemberResponseDto;
import com.cookmate.member.repository.MemberRepository;
import com.cookmate.pantry.domain.Pantry;
import com.cookmate.pantry.dto.PantryRequestDto;
import com.cookmate.pantry.repository.PantryRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import javax.print.attribute.standard.Media;
import java.lang.reflect.Array;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class PantryIntegrationTest {

    @Autowired
    PantryRepository pantryRepository;
    @Autowired
    IngredientRepository ingredientRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    JwtProvider jwtProvider;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    private MockMvc mvc;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("회원,기본재료,pantry 메소드 테스트")
    void IntegrationTest() throws Exception{

        // 1. 회원 생성
        Set<Cuisine> cuisine = new HashSet<>();
        cuisine.add(Cuisine.양식);
        cuisine.add(Cuisine.한식);

        // 회원가입
        MemberRequestDto.JoinRequest joinRequest = new MemberRequestDto.JoinRequest(
                "kalina",
                "a12345",
                "앵그리",
                true,
                "a8b3c9d2e4f7g1h6i0j5k9l2m8n4o7p1q6r0s5"
        );

        String joinContent = objectMapper.writeValueAsString(joinRequest);

        mvc.perform(MockMvcRequestBuilders.post("/api/user/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(joinContent))
                .andExpect(status().isCreated());

        assertThat(memberRepository.count()).isEqualTo(1);
        Optional<Member> member = memberRepository.findByLoginId("kalina");

        // 회원 로그인
        MemberRequestDto.LoginRequest loginRequest = new MemberRequestDto.LoginRequest(
                "kalina",
                "a12345"
        );

        String loginContent = objectMapper.writeValueAsString(loginRequest);

        MvcResult result = mvc.perform(MockMvcRequestBuilders.post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginContent))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        MemberResponseDto.JwtTokenResponse tokenResponse = objectMapper.readValue(responseBody, MemberResponseDto.JwtTokenResponse.class);
        String token = tokenResponse.accessToken();

        // 회원 프로필 등록
        MemberRequestDto.ProfileRequest profileRequest = new MemberRequestDto.ProfileRequest(
                Member.Sex.남,
                cuisine,
                15
        );

        String profileContent = objectMapper.writeValueAsString(profileRequest);

        mvc.perform(MockMvcRequestBuilders.post("/api/user/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profileContent)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated());

        member = memberRepository.findByLoginId("kalina");
        assertThat(member.get().getAge()).isEqualTo(15);
        // ===== 회원 등록 완료 ======

        // 2. 기본 재료 정보 등록
        List<IngredientRequestDto.CreateRequest> ingredientRequestList = createRequests();

        for (IngredientRequestDto.CreateRequest request : ingredientRequestList) {
            String content = objectMapper.writeValueAsString(request);
            mvc.perform(MockMvcRequestBuilders.post("/api/admin/ingredient")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(content)
                            .header("Authorization", "Bearer"+" "+token))
                    .andExpect(status().isCreated())
                    .andReturn();
        }

        long ingredientCount = ingredientRepository.count();
        assertThat(ingredientCount).isEqualTo(ingredientRequestList.size());
        // ===== 기본 재료 정보 등록 완료 ======

        // 3. Patry 등록
        List<PantryRequestDto.CreateRequest> pantryRequestList = pantryList();

        for (PantryRequestDto.CreateRequest request : pantryRequestList) {
            String content = objectMapper.writeValueAsString(request);
            mvc.perform(MockMvcRequestBuilders.post("/api/user/pantry")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(content)
                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isCreated());
        }

        long pantryCount = pantryRepository.count();
        assertThat(pantryCount).isEqualTo(ingredientRequestList.size());

        Pantry targetPantry = pantryRepository.findByName("대파");
        Long targetId = targetPantry.getId();

        // 4. Pantry 수정
        PantryRequestDto.UpdateRequest updateRequest = PantryRequestDto.UpdateRequest.builder()
                .purchaseDate(LocalDate.now().plusDays(1))
                .storageType(StorageType.냉동)
                .quantity(500)
                .expiryDate(LocalDate.now().plusMonths(1))
                .build();

        String content = objectMapper.writeValueAsString(updateRequest);
        mvc.perform(MockMvcRequestBuilders.patch("/api/user/pantry/{id}", targetId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)
                .header("Authorization", "Bearer "+token))
                .andExpect(status().isOk());

        assertThat(targetPantry.getPurchaseDate()).isEqualTo(LocalDate.now().plusDays(1));
        assertThat(targetPantry.getQuantity()).isEqualTo(500);

        // 5. Pantry 삭제
        mvc.perform(MockMvcRequestBuilders.delete("/api/user/pantry/{id}", targetId)
                        .header("Authorization", "Bearer "+token))
                .andExpect(status().isOk());

        assertThat(pantryRepository.count()).isEqualTo(2);


    }



    List<IngredientRequestDto.CreateRequest> createRequests() {
        IngredientRequestDto.CreateRequest createRequest1 = IngredientRequestDto.CreateRequest.builder()
                .name("대파")
                .defaultExpiry(10)
                .frozenExpiry(15)
                .ambientExpiry(5)
                .refrigeratedExpiry(20)
                .ingredientCategory(IngredientCategory.채소류)
                .build();

        IngredientRequestDto.CreateRequest createRequest2 = IngredientRequestDto.CreateRequest.builder()
                .name("양파")
                .defaultExpiry(20)
                .frozenExpiry(40)
                .ambientExpiry(20)
                .refrigeratedExpiry(30)
                .ingredientCategory(IngredientCategory.채소류)
                .build();

        IngredientRequestDto.CreateRequest createRequest3 = IngredientRequestDto.CreateRequest.builder()
                .name("소고기")
                .defaultExpiry(5)
                .frozenExpiry(15)
                .ambientExpiry(5)
                .refrigeratedExpiry(20)
                .ingredientCategory(IngredientCategory.육류)
                .build();

        return List.of(createRequest1, createRequest2, createRequest3);
    }

    List<PantryRequestDto.CreateRequest> pantryList() {

        // 기본 재료 정보가 ㅇ, 만료일이 x
        PantryRequestDto.CreateRequest pantryCreateRequest1 = PantryRequestDto.CreateRequest.builder()
                .name("대파")
                .purchaseDate(LocalDate.now())
                .storageType(StorageType.상온)
                .quantity(600)
                .unit(Unit.G)
                .build();

        // 기본 재료 정보 ㅇ, 만료일 ㅇ
        PantryRequestDto.CreateRequest pantryCreateRequest2 = PantryRequestDto.CreateRequest.builder()
                .name("양파")
                .purchaseDate(LocalDate.now())
                .expiryDate(LocalDate.now().plusDays(5))
                .storageType(StorageType.기본)
                .quantity(10)
                .unit(Unit.G)
                .build();

        // 기본 재료 정보 x, 만료일 ㅇ
        PantryRequestDto.CreateRequest pantryCreateRequest3 = PantryRequestDto.CreateRequest.builder()
                .name("돼지고기")
                .purchaseDate(LocalDate.now())
                .expiryDate(LocalDate.now().plusDays(10))
                .storageType(StorageType.냉장)
                .quantity(600)
                .unit(Unit.G)
                .build();


        /*
        // 기본 재료 정보 x, 만료일 x -> 예외 오류 발생
        PantryRequestDto.CreateRequest pantryCreateRequest4 = PantryRequestDto.CreateRequest.builder()
                .name("양고기")
                .purchaseDate(LocalDate.now())
                .storageType(StorageType.냉장)
                .quantity(600)
                .unit(Unit.G)
                .build();

        return List.of(pantryCreateRequest1, pantryCreateRequest2, pantryCreateRequest3, pantryCreateRequest4);
         */

        return List.of(pantryCreateRequest1, pantryCreateRequest2, pantryCreateRequest3);
    }
}
