package com.cookmate.cookmate.member;

import com.cookmate.global.security.JwtProvider;
import com.cookmate.global.type.Cuisine;
import com.cookmate.member.domain.Member;
import com.cookmate.member.dto.MemberRequestDto;
import com.cookmate.member.dto.MemberResponseDto;
import com.cookmate.member.repository.MemberRepository;
import com.cookmate.member.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import tools.jackson.databind.ObjectMapper;

import javax.print.attribute.standard.Media;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
// 서버 띄우지 않고도 가짜로 HTTP 요청/응답을 흉내 내는 도구
@AutoConfigureMockMvc
public class MemberIntegrationTest {

    @Autowired
    private JwtProvider jwtProvider;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private MemberService memberService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mvc;



    @DisplayName("jwtProvider 메소드 테스트")
    @Test
    void jwtTest() {

        Set<Cuisine> cuisine = new HashSet<>();
        cuisine.add(Cuisine.한식);
        cuisine.add(Cuisine.중식);

        // 회원가입 요청 생성
        MemberRequestDto.JoinRequest joinRequest = new MemberRequestDto.JoinRequest(
                "kalina",
                "a12345",
                "앵그리",
                true,
                "a8b3c9d2e4f7g1h6i0j5k9l2m8n4o7p1q6r0s5"
        );

        Long memberId = memberService.join(joinRequest);

        MemberRequestDto.ProfileRequest profileRequest = new MemberRequestDto.ProfileRequest(
                Member.Sex.남,
                cuisine,
                15
        );

        memberService.createProfile(memberId, profileRequest);
        Member member = memberRepository.findById(memberId).
                orElseThrow(() -> new IllegalArgumentException("memberId not found"));

        assertThat(member.getSex()).isEqualTo(Member.Sex.남);

        // 로그인 요청 생성
        MemberRequestDto.LoginRequest loginRequest = new MemberRequestDto.LoginRequest(
                "kalina",
                "a12345"
        );

        MemberResponseDto.JwtTokenResponse token = memberService.login(loginRequest);
        System.out.println(token);

        Long validId = jwtProvider.getMemberIdFromToken(token.accessToken());
        String role = jwtProvider.getRoleFromToken(token.accessToken());
        Boolean isValid = jwtProvider.validateToken(token.accessToken());

        assertThat(validId).isEqualTo(memberId);
        assertThat(role).isEqualTo("ADMIN");
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("MemberService 메소드 테스트")
    // @WithMockUser(username = "1", roles = "ADMIN")
    void memberServiceTest() throws Exception{

        Set<Cuisine> cuisine = new HashSet<>();
        cuisine.add(Cuisine.양식);
        cuisine.add(Cuisine.한식);

        // 회원가입
        MemberRequestDto.JoinRequest joinRequest = new MemberRequestDto.JoinRequest(
                "kalina",
                "a12345",
                "앵그리",
                false,
                "a8b3c9d2e4f7g1h6i0j5k9l2m8n4o7p1q6r0s5"
        );

        String joinContent = objectMapper.writeValueAsString(joinRequest);

        mvc.perform(MockMvcRequestBuilders.post("/api/user/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(joinContent))
                .andExpect(status().isCreated());

        assertThat(memberRepository.count()).isEqualTo(1);
        Optional<Member> member = memberRepository.findByLoginId("kalina");

        // 로그인
        MemberRequestDto.LoginRequest loginRequest = new MemberRequestDto.LoginRequest(
                "kalina",
                "a12345"
        );

        String loginContent = objectMapper.writeValueAsString(loginRequest);

        // *HTTP 통신은 상태를 기억하지 않는 stateless 이기 때문에 다음 api를 요청해도 role이 저장되지 않음 그래서 MvcResult로 token 값을 가져오자
        MvcResult result = mvc.perform(MockMvcRequestBuilders.post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginContent))
                .andExpect(status().isOk())
                .andReturn();

        // 응답 body의 JSON 문자열 꺼내기
        String responseBody = result.getResponse().getContentAsString();
        // JSON 문자열을 JwtTokenResponse 객체로 역직렬화
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
















    }

    












}
