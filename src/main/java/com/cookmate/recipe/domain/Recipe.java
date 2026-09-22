package com.cookmate.recipe.domain;
import com.cookmate.global.type.Cuisine;
import com.cookmate.member.domain.Member;
import com.cookmate.recipe.dto.RecipeIngredientResponseDto;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recipe")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    String title;
    @Column(nullable = false)
    String content;

    String imageUrl;
    String source;
    Integer cost;
    String cookingTime;
    Cuisine cuisine;

    @Enumerated(EnumType.STRING)
    private Level level;

    /* 
    @Builder.Default는 빈 리스트로 안전하게 초기화 해두는것
    하나의 레시피는 많은 레시피 재료들을 가진다
    db에서는 왜래키로 양쪽 매핑을 하지만 java 객체 세상에서는 양쪽 클래스에 서로를 향한
    변수를 적는다 관계의 주인을 recipe_ingredient의 recipe로 알려주는것
    cascade = CascadeType.ALL은 RecipeIngredient들을 세트로 같이 저장하게 설정
     */
    @Builder.Default
    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecipeIngredient> recipeIngredients = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "guest_id", length = 100)
    private String guestId;


    public enum Level {
        Level0,
        Level1,
        Level2,
        Level3,
        Level4,
        Level5,
    }

    /*
    메소드
     */
    public static Recipe create(
            Member member,
            String guestId,
            String title,
            String content,
            String source,
            Integer cost,
            String cookingTime,
            Level level,
            Cuisine cuisine
    ) {
        return Recipe.builder()
                .member(member)
                .guestId(guestId)
                .title(title)
                .content(content)
                .source(source)
                .cost(cost)
                .cookingTime(cookingTime)
                .level(level)
                .cuisine(cuisine)
                .build();
    }

    public Recipe update(
            String title,
            String content,
            String source,
            Integer cost,
            String cookingTime,
            Level level,
            Cuisine cuisine
    ) {
        if (title != null) this.title = title;
        if (content != null) this.content = content;
        if (source != null) this.source = source;
        if (cost != null) this.cost = cost;
        if (cookingTime != null) this.cookingTime = cookingTime;
        if (level != null) this.level = level;
        if (cuisine != null) this.cuisine = cuisine;
        return this;
    }

}
