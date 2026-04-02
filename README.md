# Gen-Coupon (자동 쿠폰 발행 시스템)

Gen-Coupon은 주문 데이터를 수집하여 조건에 따라 스탬프를 적립하고, 지정된 마일스톤 달성 시 쿠폰을 자동으로 발급 및 알림톡으로 전송하는 자동화 관리자 시스템입니다.

## 🚀 주요 기능

- **주문 수집 자동화 (Playauto 연동)**
  - 설정된 스케줄러를 통해 주기적으로 Playauto에서 주문 내역을 수집합니다.
  - 제외 매입처 키워드 등을 설정하여 스탬프 적립 대상에서 제외할 수 있습니다.
  
- **스탬프 적립 및 관리**
  - 설정된 최소 주문 금액 이상 결제 시 스탬프를 적립합니다.
  - 고객별 스탬프 보유 현황을 추적하고 모니터링합니다.

- **쿠폰 자동 발급 (ImWeb 연동)**
  - 스탬프가 특정 개수에 도달하는 마일스톤(정책)을 유연하게 설정할 수 있습니다.
  - 달성 시 ImWeb API를 통해 쿠폰을 자동 발급합니다.

- **알림톡 전송 (Aligo 연동)**
  - 스탬프 적립 갱신 및 쿠폰 발급 시 Aligo 알림톡 API를 통해 고객에게 메시지를 발송합니다.
  - 발송 결과를 폴링하여 성공/실패 여부를 어드민에 표시합니다.

- **관리자 웹 대시보드**
  - **주문자 현황 & 주문자 목록**: 전체 고객의 스탬프 현황, 누적 주문, 스토어별 주문 내역 등을 한눈에 확인.
  - **주문/쿠폰 내역 관리**: 필터 검색 및 이력 조회 기능.
  - **정책/스케줄러 설정**: 스탬프 발급 최소 금액, 쿠폰 마일스톤 별 템플릿 매핑, 수집 주기 등 동적 설정 기능.

## 🛠 기술 스택

- **Backend / Framework**
  - Java 21
  - Spring Boot (Spring Web, Spring Data JPA, Spring Cache)
  - QueryDSL (동적 쿼리 및 검색 용이)
  
- **Frontend**
  - HTML5, CSS3, JavaScript
  - Thymeleaf (서버 사이드 렌더링)
  - Bootstrap 5 (UI 프레임워크), Chart.js (통계 차트)

- **Database**
  - MariaDB (로컬 환경 및 Docker Compose 배포)

- **Infrastructure & Build**
  - Docker & Docker Compose
  - Gradle

## 📂 프로젝트 구조

```text
src/main/java/naeil/gen_coupon/
├── common/         # 공통 예외 처리, 상수, 외부 연동 클래스 (AligoExternal 등)
├── controller/     # Thymeleaf 뷰 렌더러 및 REST API 컨트롤러
├── dto/            # 요청/응답 DTO 및 외부 API 통신용 객체
├── entity/         # JPA 엔티티 클래스 (Customer, Order, Stamp, Coupon, Config 등)
├── enums/          # 발급 상태, 결과 메세지 등을 정의하는 Enum
├── repository/     # Spring Data JPA Repository 및 QueryDSL BooleanBuilder
├── scheduler/      # 주기적 배치 작업 (주문 수집, 알림톡 결과 갱신 등)
└── service/        # 비즈니스 로직 (주문/스탬프/쿠폰/메시지 처리)
```

## ⚙️ 설정 및 실행 방법

### 1. 환경 변수 설정
프로젝트 최상단 폴더에 `.env` 파일을 생성하고 다음 환경 변수를 알맞게 설정합니다.
```env
DB_ROOT_PASSWORD=비밀번호
DB_NAME=gen_coupon
DB_USER=DB유저명
DB_PASSWORD=DB비밀번호
```

### 2. Docker Compose로 실행
앱과 Database를 함께 컨테이너로 실행할 수 있습니다.
```bash
# 백그라운드로 애플리케이션 및 DB 실행
docker-compose up -d --build
```
서비스가 실행되면 `http://localhost:8081` (또는 설정된 포트)로 접속할 수 있습니다.

### 3. 로컬에서 직접 실행 (Gradle)
```bash
./gradlew bootRun
```
(단, 이 경우 MariaDB가 로컬 환경 혹은 컨테이너로 별도 구동 중이어야 하며, `application.yml`의 DB 설정이 맞는지 확인해야 합니다.)

## 📖 핵심 비즈니스 로직 흐름

1. **주문 데이터 수집**: `Playauto` API에서 일정 주기마다 신규 주문 내역을 조회합니다.
2. **필터링 및 적립**: 수집된 오더 중 '최소 주문 금액' 이상이면서 '차단 매입처'가 아닌 경우 고객에게 스탬프를 발급합니다.
3. **마일스톤 확인**: 고객의 누적 스탬프가 설정된 '쿠폰 마일스톤 정책'에 도달하면 스탬프 알림 대신 쿠폰을 자동 발급(`ImWeb` API 사용)합니다.
4. **결과 알림**: 스탬프 발급 또는 쿠폰 발급 결과를 각각의 알림톡 템플릿에 맞춰 `Aligo` API를 통해 전송 후 결과 상태를 시스템에 반영합니다.