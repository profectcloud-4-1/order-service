# MSA 점진적 전환 전략

## 현재 상태 (Phase 1: 모놀로식)

- **구조**: 단일 Spring Boot 애플리케이션
- **도메인**: Order, Cart, Review, Product, User, Delivery, Payment, Stock
- **통신**: 같은 JVM 내 메서드 호출 + Feign Client 준비
- **데이터베이스**: 단일 PostgreSQL DB (공유)

## 전환 전략: Modular Monolith → Microservices

**핵심 원칙**: 하나의 통합 Git Repository에서 도메인별 독립 모듈로 점진적 전환

### Phase 1: 모놀로식 준비 단계 ✅ (현재)

**목표**: MSA 전환을 위한 준비

- [x] 도메인별 패키지 분리
- [x] Feign Client 구조 준비
- [x] 내부 API (`/internal/**`) 준비
- [x] 도메인 경계 명확화
- [x] Spring Cloud OpenFeign 설정

**현재 구조**:
```
order-service-1/           ← 단일 Git Repository
├── settings.gradle       (단일 모듈)
├── build.gradle          (모든 의존성 포함)
└── src/main/java/profect/group1/goormdotcom/
    ├── order/            ← 도메인별 패키지
    ├── cart/            
    ├── review/           
    ├── product/
    ├── user/
    ├── delivery/
    ├── payment/
    └── stock/            ← 실제 도메인 존재
```

**참고**: Stock 도메인이 실제로 존재하므로 settings.gradle에 include되어 있음

### Phase 2: 멀티모듈 준비 단계 🔄

**목표**: 하나의 Repository 내에서 Gradle 멀티모듈로 전환

- [ ] Gradle 멀티모듈 구조로 변환
- [ ] 각 도메인을 독립 모듈로 분리
- [ ] 공통 라이브러리 모듈 생성
- [ ] 로컬 통신을 Feign Client로 점진 전환

**예상 구조**:
```
order-service-1/           ← 통합 Git Repository (유지)
├── settings.gradle       (멀티모듈로 변환)
│   include 'order-service'
│   include 'cart-service'
│   include 'review-service'
│   include 'product-service'
│   include 'user-service'
│   include 'delivery-service'
│   include 'payment-service'
│   include 'stock-service'
│   include 'api-gateway'  ← API Gateway (로컬용)
│   include 'common'       ← 공통 모듈
│
├── build.gradle          (루트 빌드 설정)
│
├── api-gateway/          ← API Gateway 모듈 (독립)
│   ├── build.gradle
│   └── src/main/java/profect/group1/goormdotcom/gateway/
│
├── order-service/        ← 독립 모듈
│   ├── build.gradle
│   └── src/main/java/profect/group1/goormdotcom/order/
│
├── cart-service/         ← 독립 모듈
│   ├── build.gradle
│   └── src/main/java/profect/group1/goormdotcom/cart/
│
├── review-service/       ← 독립 모듈
│   ├── build.gradle
│   └── src/main/java/profect/group1/goormdotcom/review/
│
└── common/               ← 공통 모듈
    ├── build.gradle
    ├── api/              (DTO, 공통 인터페이스)
    ├── config/           (공통 설정)
    └── security/         (인증/보안)
```

**장점**:
- ✅ 모듈 간 의존성 명확화
- ✅ 점진적 전환 가능
- ✅ Git 히스토리 유지

### Phase 3: 독립 배포 단계 🎯

**목표**: 각 모듈을 독립적으로 빌드/배포

- [ ] 각 모듈별 Dockerfile 생성
- [ ] 독립적인 CI/CD 파이프라인
- [ ] 모듈 간 통신은 Feign Client 강제
- [ ] 공유 DB → 모듈별 DB 분리 고려
- [ ] 모듈별 actuator/health check
- [ ] **Load Balancer 설정** (선택적)

**예상 구조**:
```
order-service-1/           ← 통합 Git Repository (유지)
├── .github/workflows/
│   ├── order-service.yml   (독립 CI/CD)
│   ├── cart-service.yml
│   └── review-service.yml
│
├── order-service/
│   ├── build.gradle
│   ├── Dockerfile          (독립 이미지)
│   └── src/...
│
├── cart-service/
│   ├── build.gradle
│   ├── Dockerfile          (독립 이미지)
│   └── src/...
│
└── docker-compose.yml     (통합 실행용)
    services:
      order-service:
      cart-service:
      review-service:
```

**통신 방식**:
- **Option A**: Feign Client 직접 통신 (현재 방식 유지)
  - 장점: 단순, 빠른 적용
  - 단점: 모듈별 정적 URL 필요
- **Option B**: Spring Cloud Load Balancer + 정적 서비스 목록
  - 장점: 로드 밸런싱 가능
  - 단점: 서비스 목록 직접 관리
- **Option C**: Eureka + Load Balancer (완전 MSA)
  - 장점: 완전한 서비스 디스커버리
  - 단점: 추가 인프라 복잡도

### Phase 4: 완전 분리 고려 (선택적) 🚀

**목표**: 필요시에만 완전히 독립된 Repository로 분리

> **주의**: 이 단계는 정말 필요할 때만 진행 (Git 히스토리 분리 비용 큼)

- [ ] 각 서비스를 별도 저장소로 분리 (git subtree/filter-branch)
- [ ] 독립적인 데이터베이스 (Database per Service)
- [ ] 서비스 디스커버리 (Eureka, Consul 등)
- [ ] API Gateway (Spring Cloud Gateway)
- [ ] 분산 추적 (Zipkin, Jaeger)
- [ ] 컨테이너 오케스트레이션 (Kubernetes)

**필요한 경우만**:
```
order-service/             ← 독립 git repo
├── Dockerfile
├── kubernetes/
└── src/main/java/...

cart-service/              ← 독립 git repo
├── Dockerfile
├── kubernetes/
└── src/main/java/...
```

## 현재 상태 상세

### 데이터베이스 설계
- 단일 PostgreSQL DB
- 도메인별 테이블 접두사 (예: p_order, p_cart, p_review)

### API 구조
- **External API**: `/api/v1/*` - 외부 클라이언트용
- **Internal API**: `/internal/v1/*` - 서비스 간 통신용

### Feign Client 준비 현황

| 소스 도메인 | 타겟 도메인 | 클라이언트 위치 | 상태 |
|------------|------------|----------------|------|
| Review | Order | `review/service/OrderClient.java` | ❌ 누락 |
| Review | Presigned | `review/service/PresignedClient.java` | ❌ 누락 |
| Order | Delivery | `order/client/DeliveryClient.java` | ✅ |
| Order | Payment | `order/client/PaymentClient.java` | ✅ |
| Order | Stock | `order/client/stock/StockClient.java` | ✅ |
| Cart | User | `cart/infrastructure/client/UserClient.java` | ✅ |
| Product | Stock | `product/infrastructure/client/StockService/StockClient.java` | ✅ |
| Product | Presigned | `product/infrastructure/client/PresignedService/PresignedClient.java` | ✅ |
| Payment | Order | `payment/infrastructure/client/OrderClient.java` | ✅ |
| Delivery | Order | `delivery/infrastructure/client/DeliveryOrderClient.java` | ✅ |
| User | Cart | `user/infrastructure/client/CartClient.java` | ✅ |

### 발견된 문제점

1. **도메인 경로 불일치**
   - Order: `order/client/` ❌
   - 나머지: `domain/infrastructure/client/` ✅
   - 해결: Order도 infrastructure/client로 통일 필요

2. **Review 도메인 클라이언트 누락**
   - `review/service/OrderClient.java` 삭제됨
   - `review/service/PresignedClient.java` 삭제됨
   - 서비스가 여전히 의존 중

## 다음 단계 추천

### 1. 즉시 수정 (Phase 1 완료) ⚡ ✅
   - ✅ Review 도메인 클라이언트 복구 완료
   - ✅ Order 도메인 경로 통일 완료 (`infrastructure/client`로 이동)
   - ✅ 모든 Feign 클라이언트가 `domain/infrastructure/client` 패턴으로 통일됨
   - ✅ ApiResponse vs ResponseEntity 혼용 문제 해결 완료
     - **변경**: 모든 Controller와 Feign Client를 `ApiResponse<T>`로 통일
     - **예외**: ExceptionAdvice는 `ResponseEntity<T>` 유지 (전역 예외 처리기 상속)

### 2. Phase 2 전환 준비 🎯
   - **목표**: 하나의 Git Repository 내 Gradle 멀티모듈로 전환
   - **첫 번째 후보**: Review 도메인 (독립적, 의존성 적음)
   - **절차**:
     1. `common` 코드는 각자 리포지토리에서 코드로 관리 (현재 정책)
     2. Review를 독립 모듈로 분리
     3. 다른 팀도 동일한 방식으로 전환
   - **장점**: Git 히스토리 유지, 점진적 전환
   - **참고**: `common` 모듈은 추후 협의를 통해 별도 관리 방식 마이그레이션 계획 있음

### 3. Phase 3 독립 배포 🚀
   - 모듈별 독립 Dockerfile
   - 독립 CI/CD 파이프라인
   - Feign Client로 완전 전환
   - **Load Balancer 고려** (Option B 또는 C)

### 4. Phase 4 완전 분리 ⚠️
   - **비권장**: Git 히스토리 분리 비용 큼
   - 정말 필요할 때만 고려

## API Gateway 구축 계획

### 로컬 환경용 API Gateway 🎯
**목표**: 인증, 라우팅 기능만 수행하는 간단한 Gateway
- ✅ 인증: JWT 검증 및 전파
- ✅ 라우팅: 경로 기반 서비스 라우팅
- 🎯 간단한 구성: 복잡도 최소화

### 현재 상황
- **Eureka**: ❌ 미사용 (필요없음)
- **API Gateway**: ⏳ 계획 중 (로컬 환경용)
- **현재 통신**: Feign Client 직접 통신 (모놀리스에서 동작)

### 통신 옵션 비교

| 옵션 | 시기 | 복잡도 | 장점 | 단점 |
|-----|-----|--------|------|------|
| **Option A**: 직접 통신 | Phase 1-2 | 낮음 | 단순, 빠름 | 로드밸런싱 X |
| **Option B**: Load Balancer | Phase 3 | 중간 | 로드밸런싱 | 정적 목록 관리 |
| **Option C**: Eureka + Gateway | Phase 4 | 높음 | 완전 디스커버리 | 인프라 복잡도 ↑ |

### 구현 계획
**로컬 환경용 API Gateway 구성**:
```
api-gateway/
├── build.gradle
│   - spring-cloud-starter-gateway
│   - spring-boot-starter-security
│   - JWT 라이브러리
│
└── src/main/java/profect/group1/goormdotcom/gateway/
    ├── GatewayApplication.java
    ├── config/
    │   ├── GatewayConfig.java        (라우팅 설정)
    │   └── SecurityConfig.java       (인증 설정)
    └── filter/
        └── JwtAuthenticationFilter.java  (JWT 검증 필터)
```

**라우팅 규칙**:
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: order-service
          uri: http://localhost:8081
          predicates:
            - Path=/api/v1/orders/**
        - id: cart-service
          uri: http://localhost:8082
          predicates:
            - Path=/api/v1/carts/**
        - id: review-service
          uri: http://localhost:8083
          predicates:
            - Path=/api/v1/reviews/**
```

**기능**:
- JWT 토큰 검증 (SecurityConfig)
- 인증/비인증 경로 분리
- 로컬 서비스로 라우팅
- 인증 헤더 전파

> **참고**: Eureka는 불필요. 로컬 환경에서는 정적 라우팅으로 충분

## 참고 자료

- [Strangler Fig Pattern](https://martinfowler.com/bliki/StranglerFigApplication.html)
- [Spring Cloud OpenFeign](https://spring.io/projects/spring-cloud-openfeign)
- [Database per Service Pattern](https://microservices.io/patterns/data/database-per-service.html)

