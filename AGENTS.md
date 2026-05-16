# AGENTS.md

## Project Identity

이 프로젝트는 카드 소비 리워드를 해외 ETF 소수점 자동투자로 전환하는 WON해요 서비스의 **증권 채널 서버**입니다.

중앙 기준 문서는 submodule로 연결된 `.wonpt/project-docs/` 폴더의 Markdown 파일입니다.  
AI는 코드를 수정하거나 문서를 작성할 때 반드시 `.wonpt/project-docs/`의 명세를 우선 참고합니다.

---

## Source of Truth

다음 문서를 기준 문서로 사용합니다.

- `.wonpt/project-docs/00_project_overview.md`: 프로젝트 전체 개요
- `.wonpt/project-docs/01_requirements.md`: 기능/비기능 요구사항
- `.wonpt/project-docs/02_domain_glossary.md`: 도메인 용어집
- `.wonpt/project-docs/03_business_rules.md`: 업무 규칙
- `.wonpt/project-docs/04_erd_spec_ver2.md`: ERD 명세
- `.wonpt/project-docs/05_api_spec.md`: API 명세
- `.wonpt/project-docs/06_event_message_spec.md`: SQS/Outbox/Inbox 이벤트 명세
- `.wonpt/project-docs/07_system_architecture.md`: 시스템 아키텍처
- `.wonpt/project-docs/08_wireframe_spec.md`: 와이어프레임 설명
- `.wonpt/project-docs/09_bpmn_process.md`: 업무 프로세스
- `.wonpt/project-docs/10_error_code_policy.md`: 에러코드/응답 정책
- `.wonpt/project-docs/12_git_convention.md`: Git 규칙
- `.wonpt/project-docs/13_code_convention.md`: 코드 규칙
- `.wonpt/project-docs/14_test_scenarios.md`: 테스트 시나리오
- `.wonpt/project-docs/15_ai_instruction.md`: AI 작업 지침

---

## Repository Role

이 레포는 WON해요 프로젝트의 **증권 채널 서버**입니다.

주요 책임은 다음과 같습니다.

- 증권계좌 연결/조회
- ETF 목록/상세 조회
- 자동투자 신청 및 ETF 변경
- 카드망에서 전달된 투자 전환 요청 수신
- SQS Inbox 처리
- 자동투자 실행 상태 관리
- 환전, 주문, 체결, 잔고 반영 흐름 연계
- 자동투자 실행 현황 조회
- 실패 건 재처리
- AI 챗봇 투자/ETF 요약 데이터 제공 또는 연계

---

## Mandatory Consistency Rules

기능을 추가하거나 수정할 때는 관련 문서를 함께 확인합니다.

### API 변경 시

반드시 다음 문서를 함께 검토합니다.

- `.wonpt/project-docs/05_api_spec.md`
- `.wonpt/project-docs/04_erd_spec_ver2.md`
- `.wonpt/project-docs/10_error_code_policy.md`
- `.wonpt/project-docs/14_test_scenarios.md`

### DB/ERD/Entity 변경 시

반드시 다음 문서를 함께 검토합니다.

- `.wonpt/project-docs/04_erd_spec_ver2.md`
- `.wonpt/project-docs/03_business_rules.md`
- `.wonpt/project-docs/05_api_spec.md`

### SQS/Outbox/Inbox 이벤트 변경 시

반드시 다음 문서를 함께 검토합니다.

- `.wonpt/project-docs/06_event_message_spec.md`
- `.wonpt/project-docs/07_system_architecture.md`
- `.wonpt/project-docs/14_test_scenarios.md`

### 보안/인증/권한/로그 변경 시

반드시 다음 문서를 함께 검토합니다.

- `.wonpt/project-docs/10_error_code_policy.md`
- `.wonpt/project-docs/14_test_scenarios.md`
- `.wonpt/project-docs/15_ai_instruction.md`

### 자동투자/스윕/재처리 로직 변경 시

반드시 다음 문서를 함께 검토합니다.

- `.wonpt/project-docs/03_business_rules.md`
- `.wonpt/project-docs/06_event_message_spec.md`
- `.wonpt/project-docs/05_api_spec.md`
- `.wonpt/project-docs/14_test_scenarios.md`

### 용어 충돌 시

용어는 `.wonpt/project-docs/02_domain_glossary.md` 기준으로 통일합니다.

---

## Backend Implementation Rules

- API 응답 형식은 `status`, `code`, `msg`, `data` 구조를 따릅니다.
- 에러 응답 형식은 `status`, `code`, `msg` 구조를 따릅니다.
- 에러코드는 `.wonpt/project-docs/10_error_code_policy.md`의 도메인 prefix 규칙을 따릅니다.
- 내부 API는 일반 사용자 앱에서 직접 호출할 수 없도록 설계합니다.
- 관리자 API는 관리자 권한 토큰으로만 호출 가능해야 합니다.
- `Idempotency-Key`가 필요한 API는 중복 실행을 방지해야 합니다.
- 동일 `sweepRequestId` 또는 동일 이벤트로 중복 환전, 중복 주문, 중복 체결, 중복 잔고 반영이 발생하면 안 됩니다.
- 실패 건 재처리는 `FAILED` 상태에서만 허용합니다.
- 이미 완료된 환전, 주문, 체결, 잔고 반영은 재처리 시 중복 수행하지 않습니다.

---

## Security Rules

- 카드번호, 계좌번호, CI, 전화번호, 이메일, JWT, Refresh Token, API Key, DB Password, WireGuard Key 원문을 로그나 응답에 노출하지 않습니다.
- 계좌번호와 카드번호는 반드시 마스킹된 값만 응답합니다.
- AI 챗봇 또는 LLM 프롬프트에는 민감정보 원문을 포함하지 않습니다.
- 투자 조언, 매수/매도 추천, 수익률 보장성 표현을 생성하지 않습니다.

---

## Documentation Policy

- 불확실한 내용은 임의로 확정하지 말고 `[확인 필요]`로 표시합니다.
- 문서 간 충돌은 `[문서 정합성 이슈]`로 표시합니다.
- 기존 내용을 삭제하기보다 변경 사유가 보이도록 수정합니다.
- 기능 변경 시 관련 문서도 함께 수정합니다.
- 문서 변경 후에는 수정한 파일, 함께 검토한 파일, 남은 이슈를 요약합니다.

---

## Git Rules

- 작업 순서는 이슈 생성 → 브랜치 생성 → 작업 → PR 작성입니다.
- 기능 브랜치는 `develop` 브랜치로 PR을 올립니다.
- `main` 브랜치로 직접 병합하지 않습니다.
- PR 전 로컬 빌드와 테스트를 수행합니다.
- 브랜치명은 `type/#이슈번호/기능명` 형식을 따릅니다.
- 예: `setting/#27/wonpt-docs-skill`

---

## Response Format

작업 완료 후 아래 형식으로 요약합니다.

### 수정한 파일

- 파일명:
- 수정 내용:

### 함께 검토한 파일

- 파일명:
- 검토 이유:

### 발견한 정합성 이슈

- 이슈:
- 영향 문서:
- 처리 상태:

### 확인 필요 사항

- 항목: