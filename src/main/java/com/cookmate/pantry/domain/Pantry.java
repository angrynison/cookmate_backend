package com.cookmate.pantry.domain;

import com.cookmate.global.type.IngredientCategory;
import com.cookmate.ingredient.domain.Ingredient;
import com.cookmate.member.domain.Member;
import com.cookmate.global.type.StorageType;
import com.cookmate.global.type.Unit;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/*
@NoArgsConstructor(access = AccessLeve.PROTECTED)
매개 변수가 없는 기본 생성자를 자동으로 만들어주되, 외부에서 아무나 호출하지 못하도록 접근제한자 지정

@AllArgsConstructor
모든 필드를 전달받는 생성자를 만들고
@Builder 이를 바탕으로 객체를 안전하게 조립하는 빌더 패턴을 생성
*/
@Entity
@Table(name = "pantry")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Pantry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    /*
    @ManyToOne(fetch = FetchType.LAZY) 관계형 데이터 베이스의 다대일 연관관계 정의
    LAZY는 데이터가 필요할때 비로소 가져오는 지연 로딩 전략
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id")
    private Ingredient ingredient;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "purchase_date", nullable = false)
    private LocalDate purchaseDate;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    // 수량 및 단위 (프론트에서 합쳐서 보낸 텍스트 통째로 저장: 3개, 500g)
    @Column(nullable = false, length = 100)
    private Integer quantity;


    @Enumerated(EnumType.STRING)
    @Column(name = "storage_type", nullable = false)
    private StorageType storageType;

    @Enumerated(EnumType.STRING)
    @Column(name = "ingredient_category")
    private IngredientCategory ingredientCategory;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Unit unit;

    /* Pantry 와 Ingredient 는 1대1 매핑 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id")
    private Ingredient ingredientId;

    /*
    식재료 등록, 만료일자는 따로 policy를 두어서 작성할 예정
     */
    public static Pantry create(
            Member member,
            Ingredient ingredient,
            String name,
            LocalDate purchaseDate,
            LocalDate expiryDate,
            StorageType storageType,
            Integer quantity,
            Unit unit
    ) {
        return Pantry.builder()
                .member(member)
                .ingredient(ingredient)
                .name(name)
                .purchaseDate(purchaseDate)
                .expiryDate(expiryDate)
                .storageType(storageType)
                .quantity(quantity)
                .unit(unit)
                .build();
    }
    
    /*
    식재료 업데이트
     */
    public Pantry update(
            String name,
            LocalDate purchaseDate,
            LocalDate expiryDate,
            StorageType storageType,
            Integer quantity
    ) {
        if (name != null) this.name = name;
        if (purchaseDate != null) this.purchaseDate = purchaseDate;
        if (expiryDate != null) this.expiryDate = expiryDate;
        if (storageType != null) this.storageType = storageType;
        if (quantity != null) this.quantity = quantity;

        return this;
    }


}
