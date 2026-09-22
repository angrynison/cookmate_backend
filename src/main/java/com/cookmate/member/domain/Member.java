package com.cookmate.member.domain;

import com.cookmate.global.type.Cuisine;
import com.cookmate.global.type.Role;
import com.cookmate.pantry.domain.Pantry;
import com.cookmate.recipe.domain.Recipe;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name= "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(length = 100)
    String name;

    @Column
    Integer age;

    @Column(nullable = false, unique = true)
    String loginId;
    @Column(nullable = false)
    String password;


    @Builder.Default
    @OneToMany(mappedBy = "member")
    List<Pantry> pantries = new ArrayList<Pantry>();

    @Builder.Default
    @OneToMany(mappedBy = "recipe")
    List<Recipe> recipes = new ArrayList<Recipe>();

    // 음식 선호도 다중 값을 저장하기 위한 memebr - memebr_cuisine의 many to one 매핑 테이블 생성
    @Builder.Default
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "member_cuisine",
            joinColumns = @JoinColumn(name = "member_id")
    )

    @Enumerated(EnumType.STRING)
    @Column(name = "cuisine_type")
    private Set<Cuisine> cuisines = new HashSet<>();



    @Enumerated(EnumType.STRING)
    private Sex sex;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    public enum Sex {
        남,
        녀
    }
    
    /*
    메소드
     */

    public void createProfile(Sex sex, Set<Cuisine> cuisines, int age) {
        this.sex = sex;
        this.cuisines.addAll(cuisines);
        this.age = age;
    }

    public void update(
            String name,
            String loginId,
            String password,
            Integer age,
            Set<Cuisine> cuisines
    ) {
        if (name != null) {
            this.name = name;
        }
        if (loginId != null) {
            this.loginId = loginId;
        }
        if (password != null) {
            this.password = password;
        }
        if (age != null) {
            this.age = age;
        }
        if (cuisines != null) {
            this.cuisines = cuisines;
        }
    }



}
