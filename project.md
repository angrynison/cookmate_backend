# CookMate 프로젝트 가이드

CookMate는 사용자의 냉장고(보유 식재료)를 관리하고, 유통기한을 자동 계산하며, AI(FastAPI 서버)가 추천한 레시피를 저장하는 **Spring Boot REST API 백엔드**입니다.

---

## 1. 기술 스택

| 구분 | 사용 기술 | 버전 |
|---|---|---|
| 언어 | Java | 21 (Gradle toolchain) |
| 프레임워크 | Spring Boot | 4.0.6 |
| 빌드 | Gradle (Wrapper) | 9.5.1 |
| 웹 | Spring Web MVC, Spring WebFlux(외부 호출용) | Boot 관리 |
| 검증 | Spring Validation (Jakarta Bean Validation) | Boot 관리 |
| 영속성 | Spring Data JPA (Hibernate) | Boot 관리 |
| DB | MySQL (운영/로컬), H2 인메모리 (테스트) | - |
| 인증 | Spring Security + JWT (jjwt) | jjwt 0.12.6 |
| 편의 | Lombok | Boot 관리 |
| 테스트 | JUnit 5, Mockito, MockMvc, spring-security-test | Boot 관리 |
| JSON | Jackson 3 (`tools.jackson.*` 패키지) | Boot 4 기본 |
| 외부 연동 | FastAPI AI 서버 (`http://localhost:8000`) | - |

---

## 2. 저장소 구조

```
cookmate/
├── backend/          # Spring Boot 애플리케이션 (실제 서비스 코드)
├── fronend/          # 프론트엔드 자리 (현재 비어 있음)
├── study/            # 개인 알고리즘·스레드 학습 코드 (서비스와 무관)
└── project.md        # 이 문서
```

### 2.1 backend 구조

```
backend/
├── build.gradle                     # 의존성·플러그인·Java 버전
├── settings.gradle                  # rootProject.name = 'cookmate'
├── gradlew / gradlew.bat            # Gradle Wrapper
└── src/
    ├── main/
    │   ├── java/com/cookmate/
    │   │   ├── CookmateApplication.java   # 진입점 (@SpringBootApplication)
    │   │   ├── RestClientConfig.java      # FastAPI 호출용 RestClient Bean
    │   │   ├── global/                    # 전 도메인 공통
    │   │   │   ├── Common.java            # 공통 응답/유틸 자리 (현재 비어 있음)
    │   │   │   ├── error/                 # 전역 예외 처리, 커스텀 예외
    │   │   │   ├── security/              # SecurityConfig, JwtProvider, JwtAuthenticationFilter
    │   │   │   └── type/                  # 공용 Enum (Cuisine, IngredientCategory, Role, StorageType, Unit)
    │   │   ├── member/                    # 회원 (가입·로그인·프로필)
    │   │   ├── ingredient/                # 기본 식재료 마스터 데이터 (관리자)
    │   │   ├── pantry/                    # 사용자 보유 식재료 (냉장고)
    │   │   │   └── policy/                # 유통기한 계산 정책 (전략 패턴)
    │   │   └── recipe/                    # 레시피 (AI 생성 결과 저장)
    │   └── resources/
    │       └── application.properties     # MySQL, JPA, JWT 설정
    └── test/
        ├── java/com/cookmate/cookmate/    # 도메인별 테스트
        └── resources/
            └── application.properties     # H2 인메모리 DB 설정
```

### 2.2 도메인 패키지 표준 구조

모든 도메인(`member`, `ingredient`, `pantry`, `recipe`)은 **같은 레이어 구조**를 따릅니다. 새 도메인을 추가할 때도 이 구조를 그대로 사용하세요.

```
com.cookmate.<domain>/
├── <Domain>Controller.java          # REST 엔드포인트 (패키지 루트에 위치)
├── domain/<Domain>.java             # JPA 엔티티
├── dto/<Domain>RequestDto.java      # 요청 DTO 묶음 (내부 record)
├── dto/<Domain>ResponseDto.java     # 응답 DTO 묶음 (내부 record)
├── repository/<Domain>Repository.java  # Spring Data JPA 인터페이스
└── service/
    ├── <Domain>Service.java         # 서비스 인터페이스
    └── Impl/<Domain>ServiceImpl.java   # 서비스 구현체
```

요청 흐름: `Controller → Service(인터페이스) → ServiceImpl → Repository → Entity`

### 2.3 도메인 개요

| 도메인 | 역할 | 주요 엔티티 | URL Prefix |
|---|---|---|---|
| member | 회원가입, 로그인(JWT 발급), 프로필, 수정·탈퇴 | `Member` (선호 음식 `member_cuisine` 컬렉션 테이블 포함) | `/api/user`, `/api/admin` |
| ingredient | 기본 식재료 정보(보관 방식별 유통기한 일수) 관리 | `Ingredient` | `/api/admin/ingredient` |
| pantry | 사용자 보유 식재료 CRUD, 대시보드 요약, 카테고리 조회 | `Pantry` | `/api/user/pantry` |
| recipe | FastAPI AI가 생성한 레시피 저장·수정·삭제 | `Recipe`, `RecipeIngredient` | `/api/user/recipe` |

---

## 3. 빌드 및 실행 방법

모든 명령은 `backend/` 디렉터리에서 실행합니다. Gradle을 따로 설치할 필요 없이 Wrapper를 사용합니다.

### 3.1 사전 준비

1. **JDK 21** 설치
2. **MySQL** 실행 후 데이터베이스 생성
   ```sql
   CREATE DATABASE cookmate DEFAULT CHARACTER SET utf8mb4;
   ```
3. `src/main/resources/application.properties`에 DB 계정, JWT 키 설정 (아래 3.4 참고)
4. (레시피 AI 기능 사용 시) FastAPI 서버를 `http://localhost:8000`에서 실행

### 3.2 명령어

| 목적 | Windows | macOS / Linux |
|---|---|---|
| 빌드 (테스트 포함) | `gradlew.bat build` | `./gradlew build` |
| 빌드 (테스트 제외) | `gradlew.bat build -x test` | `./gradlew build -x test` |
| 애플리케이션 실행 | `gradlew.bat bootRun` | `./gradlew bootRun` |
| 전체 테스트 | `gradlew.bat test` | `./gradlew test` |
| 특정 테스트 클래스 | `gradlew.bat test --tests "*PantryServiceTest"` | `./gradlew test --tests "*PantryServiceTest"` |
| 정리 | `gradlew.bat clean` | `./gradlew clean` |

- 실행 가능한 JAR: `build/libs/cookmate-0.0.1-SNAPSHOT.jar` → `java -jar build/libs/cookmate-0.0.1-SNAPSHOT.jar`
- 기본 포트: `8080`
- 테스트 리포트: `build/reports/tests/test/index.html`

### 3.3 IntelliJ IDEA

- `backend/build.gradle`을 Gradle 프로젝트로 import
- **Lombok 플러그인 설치 + Annotation Processing 활성화** 필수
  (Settings → Build → Compiler → Annotation Processors → Enable)
- `CookmateApplication.main()` 실행

### 3.4 설정 프로퍼티

| 키 | 설명 | 예시 |
|---|---|---|
| `spring.datasource.url` | MySQL 접속 URL | `jdbc:mysql://localhost:3306/cookmate?serverTimezone=Asia/Seoul&characterEncoding=UTF-8` |
| `spring.datasource.username` / `password` | DB 계정 | 로컬 환경별로 설정 |
| `spring.jpa.hibernate.ddl-auto` | 스키마 자동 생성 | 로컬 `update`, 테스트 `create-drop` |
| `jwt.key` | JWT 서명 키 (HS256, **32바이트 이상**) | 비공개 값 |
| `jwt.expiration` | 토큰 만료 시간(ms) | `3600000` (1시간) |
| `jwt.adminToken` | 관리자 가입 시 검증하는 서버 비밀값 | 비공개 값 |

> ⚠️ **보안 규칙:** DB 비밀번호, `jwt.key`, `jwt.adminToken`은 저장소에 커밋하지 않습니다. 환경 변수로 주입하세요.
> ```properties
> spring.datasource.password=${DB_PASSWORD}
> jwt.key=${JWT_KEY}
> jwt.adminToken=${JWT_ADMIN_TOKEN}
> ```
> 현재 `application.properties`에는 실제 값이 평문으로 들어 있으므로, 환경 변수 방식으로 옮기고 키를 교체해야 합니다.

### 3.5 테스트 환경

- `src/test/resources/application.properties`가 main 설정을 덮어써 **H2 인메모리 DB(`MODE=MySQL`)** 를 사용합니다. 테스트 실행에 MySQL은 필요 없습니다.
- 스키마는 테스트마다 `create-drop` 으로 생성·삭제됩니다.

---

## 4. 인증·인가 규칙

### 4.1 흐름

1. `POST /api/user/login` → `MemberServiceImpl.login()`이 BCrypt로 비밀번호 확인 후 `JwtProvider.createToken(memberId, role)`로 토큰 발급
2. 클라이언트는 이후 요청마다 `Authorization: Bearer <token>` 헤더 전송
3. `JwtAuthenticationFilter`(`OncePerRequestFilter`)가 토큰을 검증하고,
   - `SecurityContext`에 인증 객체 저장 (`username` = memberId 문자열, 권한 = role)
   - `request.setAttribute("memberId", memberId)` 로 요청 속성에도 저장
4. 세션은 사용하지 않음 (`SessionCreationPolicy.STATELESS`), CSRF·formLogin·httpBasic 비활성화

### 4.2 URL 접근 권한 (`SecurityConfig`)

| 경로 | 권한 |
|---|---|
| `/api/user/signup`, `/api/user/login` | 누구나 |
| `/api/user/**` | `USER`, `ADMIN` |
| `/api/admin/signup`, `/api/admin/login` | 누구나 |
| `/api/admin/**` | `ADMIN` |
| 그 외 | 인증 필요 |

**규칙:** 새 API는 반드시 위 URL 체계 안에 두세요. 일반 사용자 기능은 `/api/user/...`, 관리자 기능은 `/api/admin/...` 아래에 둡니다.

### 4.3 컨트롤러에서 로그인 회원 ID 얻기

현재 두 방식이 섞여 있습니다. **새 코드는 `@RequestAttribute("memberId")` 하나로 통일합니다.**

```java
// ✅ 표준
public ResponseEntity<...> api(@RequestAttribute("memberId") Long memberId, ...)

// ⚠️ 기존 방식 (member 컨트롤러, pantry 삭제) — 점진적으로 위 방식으로 교체
public ResponseEntity<...> api(@AuthenticationPrincipal UserDetails userDetails) {
    Long memberId = Long.parseLong(userDetails.getUsername());
}
```

---

## 5. 코딩 규칙 & 컨벤션

### 5.1 네이밍

| 대상 | 규칙 | 예시 |
|---|---|---|
| 패키지 | 소문자, 도메인 단위 | `com.cookmate.pantry` |
| 서비스 구현 패키지 | `service.Impl` (기존 구조 유지) | `pantry.service.Impl` |
| 클래스 | PascalCase + 역할 접미사 | `PantryController`, `PantryService`, `PantryServiceImpl`, `PantryRepository` |
| DTO 묶음 클래스 | `<Domain>RequestDto`, `<Domain>ResponseDto` | `PantryRequestDto` |
| DTO record | `<동작>Request`, `<내용>Response` | `CreateRequest`, `UpdateRequest`, `SummaryResponse` |
| 메서드 | camelCase, 동사로 시작 | `createPantry`, `getPantryList`, `updatePantry`, `deletePantry` |
| DB 테이블/컬럼 | snake_case | `@Table(name = "pantry")`, `@Column(name = "expiry_date")` |
| Enum 상수 | 비즈니스 값은 **한글** 사용, 기술 값은 영문 대문자 | `StorageType.냉장`, `Cuisine.한식` / `Role.USER`, `Unit.KG` |

### 5.2 Controller

- `@RestController` + `@RequestMapping("/api/...")` + `@RequiredArgsConstructor`
  (`@Controller` 사용 금지 — `RecipeController`는 수정 대상)
- 의존성은 **서비스 인터페이스**로 주입 (`private final PantryService pantryService;`)
- 반환 타입은 `ResponseEntity<T>`
  - 생성: `ResponseEntity.status(HttpStatus.CREATED).body(id)`
  - 조회/수정: `ResponseEntity.ok(body)`
  - 삭제: 현재 `void` 반환 → 새 코드는 `ResponseEntity.noContent().build()` 권장
- HTTP 메서드는 명시적으로: `@GetMapping`, `@PostMapping`, `@PatchMapping`, `@DeleteMapping`
  (메서드 없는 `@RequestMapping`은 모든 HTTP 메서드를 받으므로 사용 금지)
- 수정은 **부분 수정 `PATCH`**, 경로 변수는 `/{pantryId}` 형식
- 요청 본문 검증이 필요하면 `@Valid @RequestBody` 사용
- 비즈니스 로직 금지 — 서비스 호출과 응답 변환만 담당

### 5.3 Service

- 인터페이스(`<Domain>Service`) + 구현체(`Impl/<Domain>ServiceImpl`) 쌍으로 작성
- 구현체: `@Service` + `@RequiredArgsConstructor` (생성자 주입, `final` 필드)
- 트랜잭션은 **`org.springframework.transaction.annotation.Transactional`** 을 사용
  (일부 코드의 `jakarta.transaction.Transactional`은 `readOnly` 등 옵션이 없으므로 교체 대상)
  - 클래스에 `@Transactional`, 조회 메서드에는 `@Transactional(readOnly = true)` 권장
- 엔티티 조회 실패: `orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."))`
- **소유권 검증 필수:** 회원 소유 데이터를 수정·삭제할 때는 반드시 소유자를 비교
  ```java
  if (!pantry.getMember().getId().equals(memberId)) {
      throw new AccessDeniedException("해당 식재료에 대한 권한이 없습니다.");
  }
  ```
- 변경 감지(Dirty Checking) 사용: 수정 시 `save()`를 다시 호출하지 않고 엔티티의 `update()` 메서드만 호출
- 반환값: 생성/수정은 엔티티 ID(`Long`), 조회는 Response DTO

### 5.4 Entity (domain)

```java
@Entity
@Table(name = "pantry")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Pantry { ... }
```

- `@Setter` 사용 금지. 상태 변경은 의미 있는 이름의 메서드로만 (`update(...)`, `createProfile(...)`)
- 생성은 **정적 팩토리 메서드 `create(...)`** 사용 (내부에서 builder 호출)
- `update(...)`는 **null이 아닌 값만 반영** (PATCH 의미)
  ```java
  if (name != null) this.name = name;
  ```
- PK: `@Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;`
- 연관관계: `@ManyToOne(fetch = FetchType.LAZY)` + `@JoinColumn(name = "xxx_id")` (기본 LAZY)
- Enum 컬럼: 반드시 `@Enumerated(EnumType.STRING)` (ORDINAL 금지)
- 컬렉션 필드 초기화 시 `@Builder.Default` 사용
- 필드 접근제한자는 `private`으로 통일
- 같은 컬럼을 두 필드로 매핑하지 않음 (예: `Pantry.ingredient`와 `ingredientId` 중복 → 수정 대상)

### 5.5 DTO

- 도메인별로 Request/Response **묶음 클래스 1개**를 만들고, 내부에 `record`를 정의
  ```java
  public final class PantryRequestDto {
      private PantryRequestDto() {}   // 인스턴스화 방지

      public record CreateRequest(
              @NotBlank(message = "식재료 이름은 필수입니다") String name,
              @NotNull(message = "구매일은 필수입니다") LocalDate purchaseDate,
              ...
      ) {}
  }
  ```
- 사용 시 `PantryRequestDto.CreateRequest`처럼 바깥 클래스명으로 참조
- 검증 메시지는 **한글**로 작성
- Response record에는 엔티티 → DTO 변환용 정적 메서드 `from(Entity)` 작성
  ```java
  public static PantryResponse from(Pantry pantry) { ... }
  ```
- 엔티티를 컨트롤러 응답으로 직접 반환 금지

### 5.6 Repository

- `JpaRepository<Entity, Long>` 상속 인터페이스
- 단순 조회는 메서드 이름 쿼리: `findByName`, `existsByLoginId`, `countByMemberId`
- 단건 조회 결과는 `Optional<T>` 반환
- 복잡한 조건은 `@Query`(JPQL) + `@Param`
  - JPQL 파라미터 이름과 `@Param` 이름을 **정확히 일치**시킬 것
  - 연관 엔티티를 ID로 비교할 때는 `p.member.id = :memberId` 형식 사용 (`p.member = :memberId` 금지)

### 5.7 예외 처리

- 전역 처리: `global/error/GlobalExceptionHandler` (`@RestControllerAdvice`)
- 에러 응답 형식:
  ```json
  { "errorCode": "INVALID_EXPIRY_DATE", "errorMessage": "식재료의 유통기한 정보가 존재하지 않습니다" }
  ```
- 도메인 고유 오류는 `global/error`에 `RuntimeException`을 상속한 커스텀 예외로 정의하고, 핸들러에 매핑 추가
- 사용 중인 표준 예외
  | 상황 | 예외 |
  |---|---|
  | 잘못된 입력, 존재하지 않는 데이터 | `IllegalArgumentException` |
  | 상태 제약 위반 (예: 냉장고 200개 초과) | `IllegalStateException` |
  | 타인 데이터 접근 | `AccessDeniedException` |
- 새 코드에서 `new RuntimeException(...)` 직접 사용은 지양 (recipe 서비스는 수정 대상)
- `IllegalArgumentException` 등 표준 예외에 대한 전역 핸들러가 아직 없으므로 추가 시 이 파일에 작성

### 5.8 도메인 정책 (전략 패턴)

- 교체 가능한 비즈니스 규칙은 `<domain>/policy/` 아래 **인터페이스 + `@Component` 구현체**로 분리
  - 예: `ExpiryDatePolicy` ← `SeasonalExpiryDatePolicy`
- 유통기한 결정 우선순위 (`PantryServiceImpl.createPantry`)
  1. 요청에 들어온 `expiryDate`
  2. 기본 식재료(`Ingredient`)의 보관 방식별 일수로 계산 (상온은 월별 계절 보정 비율 적용)
  3. 둘 다 없으면 예외

### 5.9 외부 API (FastAPI) 호출

- `RestClientConfig`의 `RestClient fastApiRestClient` Bean을 **서비스에서 주입**하여 사용
- 컨트롤러에서 직접 외부 호출 금지
- base URL은 하드코딩 대신 `application.properties` 값(예: `fastapi.base-url`)으로 분리하는 것을 권장

### 5.10 주석·로그

- 주석은 **한글**, 메서드 위에 `// 무엇을 하는지` 한 줄 작성 (기존 스타일)
- 로그는 Lombok `@Slf4j`의 `log.info/error` 사용 — `System.out.println` 금지
  (`JwtAuthenticationFilter`의 출력문은 제거 대상)
- 사용하지 않는 import(예: `org.apache.coyote.Response`, `jdk.jfr.Category`) 정리

### 5.11 포맷

- 들여쓰기 4칸 스페이스 (`CookmateApplication`만 탭 — 스페이스로 통일)
- 중괄호는 같은 줄에서 열기 (K&R)
- 메서드 사이 빈 줄은 1줄, 파일 끝의 연속 빈 줄 제거

---

## 6. 테스트 규칙

### 6.1 위치·이름

- 위치: `src/test/java/com/cookmate/cookmate/<domain>/`
- 이름: `<대상>ControllerTest`, `<대상>ServiceTest`, `<대상>IntegrationTest`
- 테스트 메서드에는 `@DisplayName("한글 설명")` 필수

### 6.2 종류별 작성법

| 종류 | 어노테이션 | 대상 | 비고 |
|---|---|---|---|
| 서비스 단위 테스트 | `@ExtendWith(MockitoExtension.class)` | `ServiceImpl` | `@InjectMocks` 구현체, `@Mock` Repository·Policy. Spring 컨텍스트 미사용 |
| 컨트롤러 슬라이스 테스트 | `@WebMvcTest(XxxController.class)` | Controller | `MockMvc` + `@MockitoBean` 서비스 |
| 통합 테스트 | `@SpringBootTest` + `@AutoConfigureMockMvc` + `@Transactional` | 전체 흐름 | H2 사용, `JwtProvider.createToken()`으로 테스트 토큰 생성 |

- Spring Boot 4 / Jackson 3 패키지에 주의
  - `org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest`
  - `org.springframework.test.context.bean.override.mockito.MockitoBean`
  - `tools.jackson.databind.ObjectMapper`
- Mockito는 BDD 스타일: `given(...).willReturn(...)`, 검증은 `verify(...)`
- 단언은 AssertJ `assertThat(...)`
- 테스트 데이터 Enum은 실제 한글 값 사용 (`IngredientCategory.채소류`)
- 빈 껍데기 테스트 클래스(`PantryControllerTest`, `RecipeControllerTest`, `RecipeServiceTest`)는 기능 구현 시 함께 작성

---

## 7. API 요약

| 메서드 | 경로 | 설명 | 권한 |
|---|---|---|---|
| POST | `/api/user/signup` | 회원가입 | 공개 |
| POST | `/api/user/login` | 로그인 → `{ accessToken }` | 공개 |
| POST | `/api/user/profile` | 최초 프로필 등록 (성별, 선호 음식, 나이) | USER |
| GET | `/api/user/info` | 내 정보 조회 | USER |
| PATCH | `/api/user` | 회원 정보 수정 | USER |
| DELETE | `/api/user` | 회원 탈퇴 | USER |
| POST | `/api/admin` | 관리자 가입 (`isAdmin`, `adminToken` 필요) | ⚠️ 현재 `/api/admin/signup`만 공개라 경로 불일치 |
| POST | `/api/admin/login` | 관리자 로그인 | 공개 |
| GET | `/api/admin/ingredient` | 기본 식재료 목록 | ADMIN |
| GET | `/api/admin/ingredient/category?category=` | 카테고리별 기본 식재료 | ADMIN |
| POST | `/api/admin/ingredient` | 기본 식재료 등록 | ADMIN |
| PATCH | `/api/admin/ingredient/{ingredientId}` | 기본 식재료 수정 | ADMIN |
| DELETE | `/api/admin/ingredient/{ingredientId}` | 기본 식재료 삭제 | ADMIN |
| GET | `/api/user/pantry/summary` | 대시보드 요약 (전체/임박/신선) | USER |
| GET | `/api/user/pantry?category=` | 카테고리별 보유 식재료 | USER |
| POST | `/api/user/pantry` | 보유 식재료 등록 (회원당 최대 200개) | USER |
| PATCH | `/api/user/pantry/{pantryId}` | 보유 식재료 수정 | USER |
| DELETE | `/api/user/pantry/{pantryId}` | 보유 식재료 삭제 | USER |
| POST | `/api/user/recipe/register` | AI 레시피 저장 (`X-Guest-Id` 헤더로 비회원 지원) | USER |
| PATCH | `/api/user/recipe/update/{recipeId}` | 레시피 수정 | USER |
| DELETE | `/api/user/recipe/delete/{recipeId}` | 레시피 삭제 | USER |

> URL 규칙: 리소스 이름은 단수형, 동작은 HTTP 메서드로 표현합니다. recipe의 `/register`, `/update`, `/delete` 같은 동사형 경로는 새 API에서 사용하지 않습니다.

---

## 8. 알려진 문제 (정리 필요 목록)

코드 분석에서 발견된 항목입니다. 수정 시 위 규칙에 맞춰 함께 정리하세요.

| 위치 | 문제 |
|---|---|
| `application.properties` | DB 비밀번호·JWT 키가 평문으로 저장됨 → 환경 변수로 이전 후 키 교체 |
| `Recipe` / `RecipeIngredient` | `mappedBy="recipe"`인데 실제 필드명은 `recipeId` → 기동 실패 가능 |
| `Member.recipes` | `mappedBy = "recipe"`인데 `Recipe`의 회원 필드명은 `member` → `mappedBy = "member"`로 수정 |
| `Pantry` | `ingredient_id` 컬럼을 `ingredient`, `ingredientId` 두 필드가 중복 매핑 |
| `PantryRepository.countBysoonDate` | `:memebrId` 오타, `p.member`를 Long과 비교 |
| `MemberServiceImpl.deleteMember` | `pantryRepository.deleteById(회원ID)` → `deleteByMemberId` 사용해야 함, `@Transactional` 누락 |
| `MemberServiceImpl.updateMember` | 새 비밀번호를 암호화하지 않고 저장 |
| `RecipeServiceImpl.requestRecipeToAi` | AI 응답을 받고 사용하지 않음 |
| `RecipeServiceImpl.deleteRecipe` / `updateRecipe` | 소유권 검증 없음 |
| `RecipeController` | `@Controller` → `@RestController` |
| `IngredientController` | 조회 API가 메서드 없는 `@RequestMapping` 사용 |
| `SecurityConfig` vs `MemberController` | 관리자 가입 경로 `/api/admin` vs 허용 경로 `/api/admin/signup` 불일치 |
