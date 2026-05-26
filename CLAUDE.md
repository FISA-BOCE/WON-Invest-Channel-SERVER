# WON해요 프로젝트 - Claude Code 컨텍스트

## 프로젝트 개요
카드-증권 자동투자 연동 서비스. 카드 포인트(리워드)를 증권 ETF에 자동 투자.

## 서버 구성
| 서버 | 패키지 루트 | 역할 |
|------|------------|------|
| WON-Invest-Channel | `com.woorifisa.investchannel` | 고객 앱 → 채널계 API (프론트 호출) |
| WON-Invest-Core | `com.woorifisa.investcore` | 채널계 → 계정계 API (internal) |
| WON-Card-Channel | `com.woorifisa.cardchannel` | 카드 채널계 |
| WON-Card-Core | `com.woorifisa.cardcore` | 카드 계정계 |
| WON-Common | `com.woorifisa.common` | CI 기반 카드-증권 매핑 |

---

## 패키지 구조 (IMPORTANT)

```
src/main/java/com/woorifisa/{server}
├── global
│   ├── config
│   ├── entity        # BaseTimeEntity 등 공통 엔티티
│   ├── exception
│   │   ├── code/     # ErrorCode.java (interface), CommonErrorCode.java
│   │   └── handler/  # BusinessException.java, GlobalExceptionHandler.java
│   ├── response      # ApiResponse.java, ErrorResponse.java, SuccessStatus.java
│   └── util
│
├── domain
│   └── {business-domain}
│       ├── api           # Controller (@RestController)
│       ├── service       # Service (@Service)
│       ├── model         # JPA Entity + Enum (model이 entity 역할)
│       ├── dto
│       │   ├── request
│       │   └── response
│       ├── repository    # JpaRepository
│       ├── external      # OpenFeign 인터페이스 (타 서버 호출)
│       ├── event         # 이벤트 발행/수신
│       └── exception     # 도메인별 ErrorCode enum
│
└── {Server}Application.java
```

---

## 코드 컨벤션 (YOU MUST FOLLOW)

### 네이밍
- 변수: `camelCase`
- 클래스: `PascalCase`
- 패키지: `소문자`
- 상수: `UPPER_SNAKE_CASE`

### 클래스명 규칙
- Controller → `{Domain}Api` (외부), `Internal{Domain}Api` (내부)
- Service → `{Domain}Service`
- Repository → `{Domain}Repository`
- Request DTO → `{Action}{Domain}Request`
- Response DTO → `{Action}{Domain}Response`
- 도메인 에러 → `{Domain}ErrorCode`
- Feign client → `{TargetServer}{Domain}Api`

### API URL 규칙
- 리소스 복수형: `/accounts`, `/users`
- kebab-case: `/invest-accounts`
- 외부 사용자: `/api/...`
- 서버 내부: `/internal/...`
- 관리자: `/admin/...`
- path variable은 camelCase: `/{accountId}`
- 필터링/검색은 query string

### 메서드명 규칙
- Controller: `create~`, `update~`, `get~`, `delete~`
- Service: 비즈니스 행위 기준 (`openNewInvestAccount`, `linkInvestAccount`)

### DTO
- Java `record` 사용
- 엔티티와 1:1 매핑 금지, 필요한 필드만

### Entity (model)
- `BaseTimeEntity` 상속
- 클래스명 단수형
- 필드 순서: ID → 일반 필드 → 연관관계
- `FetchType.LAZY` 기본

### 예외 처리
- 도메인 에러: `{Domain}ErrorCode implements ErrorCode`
- 서비스에서: `throw new BusinessException({Domain}ErrorCode.XXX)`
- 글로벌 핸들러: `GlobalExceptionHandler`

### 응답 구조
```java
// 성공
ApiResponse.of(SuccessStatus.CREATED, data)   // 201
ApiResponse.of(SuccessStatus.OK, data)         // 200

// 에러 코드 형식: {DOMAIN}_{STATUS}_{SEQ}
// 예: INVST_400_001, CARD_404_001, COM_500_001
```

---

## 현재 구현할 API

### API (1) - 채널계 (프론트 → 채널계)
- **서버**: WON-Invest-Channel (`com.woorifisa.investchannel`)
- **endpoint**: `POST /api/invest/accounts/new`
- **역할**: 프론트에서 받은 증권계좌 개설 요청을 검증 후 Core 서버로 전달
- **처리**:
    1. 입력값 검증 (비밀번호 일치 여부, 필수 약관 포함)
    2. JWT에서 user_uuid 추출
    3. Core 서버 `/internal/invest/accounts/new` Feign 호출
    4. Core 응답 그대로 반환

### API (2) - 계정계 (채널계 → 계정계)
- **서버**: WON-Invest-Core (`com.woorifisa.investcore`)
- **endpoint**: `POST /internal/invest/accounts/new`
- **역할**: 실제 DB 저장 및 상태 업데이트
- **처리**:
    1. 입력값 검증
    2. JWT 유효성 확인 및 user_uuid 추출
    3. `통합 사용자 매핑`.invst_connected_status = CONNECTED 중복 확인
    4. 약관 동의 검증 (INVEST_BASIC 필수)
    5. `증권망 고객 원본 정보` 생성 (tel_enc, email_enc 암호화)
    6. `증권 계좌 원본 정보` 생성 + invst_account_uuid 발급
    7. `통합 사용자 매핑`.invst_connected_status = CONNECTED 업데이트
    8. `채널계 조회용 증권 계좌` 동기화 (account_no_display 마스킹, account_status)
    9. 응답 반환

---

## Request/Response 명세

### Request Body (공통)
```json
{
  "phoneNumber": "010-1234-5678",
  "customerName": "홍길동",
  "accountPassword": "pass1234!",
  "accountPasswordConfirm": "pass1234!",
  "email": "hong@example.com",
  "agreedTerms": ["INVEST_BASIC", "INVEST_AUTO"]
}
```

### Response (채널계 - investAccountUuid)
```json
{
  "status": 201,
  "message": "증권계좌 개설이 완료되었습니다.",
  "data": {
    "investAccountUuid": "ACC-UUID-5678-EFGH",
    "accountNoDisplay": "123-***-***456",
    "accountStatus": "ACTIVE",
    "investConnectedStatus": "CONNECTED",
    "openedAt": "2026-05-12T10:00:00Z"
  }
}
```

### Response (계정계 - invstAccountUuid)
```json
{
  "status": 201,
  "message": "증권계좌 개설이 완료되었습니다.",
  "data": {
    "invstAccountUuid": "ACC-UUID-5678-EFGH",
    "accountNoDisplay": "123-***-***456",
    "accountStatus": "ACTIVE",
    "invstConnectedStatus": "CONNECTED",
    "openedAt": "2026-05-12T10:00:00Z"
  }
}
```

---

## 에러 코드

### 공통
| Code | HTTP | Message |
|------|------|---------|
| COM_400_001 | 400 | 요청 형식이 올바르지 않습니다. |
| AUTH_401_001 | 401 | 인증이 필요합니다. |
| AUTH_401_002 | 401 | 토큰이 만료되었습니다. |
| AUTH_403_001 | 403 | 해당 요청에 대한 권한이 없습니다. |
| COM_500_001 | 500 | 서버 내부 오류가 발생했습니다. |

### 계정 도메인 (채널계: INVEST_, 계정계: INVST_)
| Code | HTTP | Message | 조건 |
|------|------|---------|------|
| INVEST_400_001 / INVST_400_001 | 400 | 입력값 형식이 올바르지 않습니다. | 필수 필드 누락 또는 형식 오류 |
| INVEST_400_002 / INVST_400_002 | 400 | 비밀번호가 일치하지 않습니다. | password != passwordConfirm |
| INVEST_400_003 / INVST_400_003 | 400 | 이미 연결된 증권계좌가 존재합니다. | invst_connected_status = CONNECTED |
| INVEST_400_004 / INVST_400_004 | 400 | 필수 약관에 동의하지 않았습니다. | agreedTerms에 INVEST_BASIC 누락 |

---

## 주의사항
- `model` 패키지가 entity 역할 (domain과 중복이라 model로 명명)
- Channel 서버는 DB 직접 접근 없이 external(Feign)로 Core 호출
- Core 서버는 internal API prefix 사용
- 비밀번호, 전화번호, 이메일은 암호화 저장 (AES 등)
- account_no_display는 마스킹 처리 (123-***-***456 형태)