# CookMate — 에이전트 지침

이 파일에는 **이 저장소에만 해당하는 규칙**만 둔다.
프로젝트의 상세 구조·규칙·API 목록은 `project.md`에 있으며, 아래에서 그대로 불러온다.

@project.md

---

## 1. 학습·지도 방식

- 역할, 큰 그림, 챕터 나누기, 3단계 지도, 디버깅 지도 같은 **학습 규칙은 `dev-learning-mentor` 스킬 한 곳에서 관리**한다.
- 이 파일에 학습 규칙을 다시 쓰지 않는다. (두 곳에 쓰면 한쪽만 고쳐져서 충돌한다)
- 저장소 규칙(`project.md`)과 현업 관행이 다르면 차이를 설명하되 **저장소 규칙을 따르게** 한다.

## 2. 이 저장소에서 지도할 때 기준

- 새 클래스 위치: `project.md` 2.2 도메인 표준 구조 (`Controller → Service → ServiceImpl → Repository → Entity`)
  - 전 도메인 공통이면 `global/`, 교체 가능한 비즈니스 규칙이면 `<domain>/policy/`
  - 새 API는 반드시 `/api/user/...` 또는 `/api/admin/...` 아래
- 네이밍·코드 규칙: `project.md` 5장 / 테스트 규칙: 6장
- 사용자 코드에서 `project.md` 8장 "알려진 문제"와 같은 패턴이 보이면 함께 짚는다.
- 검증은 `backend/`에서 Gradle Wrapper로 한다. 테스트는 H2라서 MySQL 없이 돈다.

```bash
./gradlew test --tests "*PantryServiceTest"
```

(Windows PowerShell: `.\gradlew.bat test --tests "*PantryServiceTest"`)

## 3. 프로젝트 핵심 요약 (빠른 참조)

자세한 내용은 `project.md`를 본다. 자주 틀리는 것만 모았다.

| 항목 | 규칙 |
|---|---|
| 스택 | Java 21, Spring Boot 4.0.6, Gradle 9.5.1, JPA/MySQL(테스트는 H2), JWT(jjwt 0.12.6), Jackson 3 |
| 도메인 | `member`, `ingredient`, `pantry`, `recipe` — 모두 같은 패키지 구조 |
| 로그인 회원 ID | `@RequestAttribute("memberId") Long memberId`로 통일 (`@AuthenticationPrincipal` 방식은 교체 대상) |
| 트랜잭션 | `org.springframework.transaction.annotation.Transactional` 사용, 조회는 `readOnly = true` |
| 엔티티 | `@Setter` 금지, 정적 팩토리 `create(...)`, PATCH용 `update(...)`는 null이 아닌 값만 반영, Enum은 `EnumType.STRING` |
| DTO | `<Domain>RequestDto` / `<Domain>ResponseDto` 묶음 클래스 안의 `record`, 응답은 `from(Entity)` |
| 소유권 | 회원 소유 데이터 수정·삭제 시 소유자 비교 후 `AccessDeniedException` |
| 외부 API | FastAPI 호출은 서비스에서 `RestClient` Bean 주입으로만 |
| 테스트 | 단위(Mockito) / 슬라이스(`@WebMvcTest` + `@MockitoBean`) / 통합(`@SpringBootTest`), `@DisplayName` 한글 필수 |
| Boot 4 패키지 | `org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest`, `tools.jackson.databind.ObjectMapper` |
| 비밀값 | DB 비밀번호·`jwt.key`·`jwt.adminToken`은 커밋 금지, 환경 변수로 주입 |
