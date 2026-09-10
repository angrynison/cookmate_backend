package com.cookmate.cookmate.pantry;

import com.cookmate.global.type.StorageType;
import com.cookmate.global.type.Unit;
import com.cookmate.ingredient.domain.Ingredient;
import com.cookmate.ingredient.repository.IngredientRepository;
import com.cookmate.member.domain.Member;
import com.cookmate.member.repository.MemberRepository;
import com.cookmate.pantry.domain.Pantry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import com.cookmate.pantry.dto.PantryRequestDto;
import com.cookmate.pantry.repository.PantryRepository;
import com.cookmate.pantry.service.Impl.PantryServiceImpl;
import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@DisplayName("Pantry 서비스 unit 테스트")
@ExtendWith(MockitoExtension.class)
@SpringBootTest
public class PantryServiceTest {

    @InjectMocks
    private PantryServiceImpl pantryService;

    @Mock
    private PantryRepository pantryRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private IngredientRepository ingredientRepository;


    @Test
    @DisplayName("재료 통계 요약 테스트")
    void getPantrySummary() {
        Long memberId = 1L;


    }

    @Test
    @DisplayName("재료추가 테스트")
    void addPantry() {

        //given
        Long member1 = 1L;
        Long member2 = 2L;
        Member mockMember1 = Member.builder().id(member1).build();
        Member mockMember2 = Member.builder().id(member2).build();

        given(memberRepository.findById(member1)).willReturn(Optional.of(mockMember1));
        given(memberRepository.findById(member2)).willReturn(Optional.of(mockMember2));

        // 재료 등록 요청 생성

        // 유통기한을 적거나, ingredient Id를 넘겨주지 않으면 설정한 IllegalArgumentException이 발생
//        PantryRequestDto.CreateRequest pantryRequest = PantryRequestDto.CreateRequest.builder()
//                .name("감자")
//                .purchaseDate(LocalDate.now())
//                .storageType(StorageType.냉장)
//                .quantity(2)
//                .unit(Unit.EA)
//                .build();

        PantryRequestDto.CreateRequest pantryRequest1 = PantryRequestDto.CreateRequest.builder()
                .name("감자")
                .purchaseDate(LocalDate.now())
                .expiryDate(LocalDate.now().plusDays(5))
                .storageType(StorageType.냉장)
                .quantity(2)
                .unit(Unit.EA)
                .build();


        PantryRequestDto.CreateRequest pantryRequest2 = PantryRequestDto.CreateRequest.builder()
                .name("당근")
                .purchaseDate(LocalDate.now())
                .expiryDate(LocalDate.now().plusDays(3))
                .storageType(StorageType.상온)
                .quantity(3)
                .unit(Unit.EA)
                .build();

        //when
        pantryService.createPantry(member1, pantryRequest1);
        pantryService.createPantry(member2, pantryRequest2);

        //then
        verify(pantryRepository, times(2)).save(any(Pantry.class));
    }

    @Test
    @DisplayName("재료정보 업데이트 단위 테스트")
    void updatePantryTest() {

        // Given
        Long memberId = 1L;
        Long pantryId = 100L;

        Member mockMember = Member.builder()
                .id(memberId)
                .build();

        Pantry mockPantry = Pantry.builder()
                .id(pantryId)
                .member(mockMember)
                .name("감자")
                .quantity(2)
                .expiryDate(LocalDate.now().plusDays(5))
                .build();

        LocalDate changeExpiry = LocalDate.now().plusDays(10);
        Integer changeQuantity = 5;

        PantryRequestDto.UpdateRequest updateRequest = PantryRequestDto.UpdateRequest.builder()
                .expiryDate(changeExpiry)
                .quantity(changeQuantity)
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(mockMember));
        given(pantryRepository.findById(pantryId)).willReturn(Optional.of(mockPantry));


        // When
        pantryService.updatePantry(memberId, pantryId, updateRequest);


        // Then
        Assertions.assertEquals(changeQuantity, mockPantry.getQuantity());
        Assertions.assertEquals(changeExpiry, mockPantry.getExpiryDate());
    }



}
