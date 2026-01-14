# BillGates - 통신 요금 정산 및 알림 시스템

대용량 통신 요금 정산과 비동기 메시지 발송을 위한 백엔드 시스템

##  목차

- [서비스 소개](#-서비스-소개)
- [프로젝트 배경](#-프로젝트-배경)
- [기술적 달성](#-기술적-달성)
- [프로젝트 일정](#-프로젝트-일정)
- [기술 스택](#️-기술-스택)
- [실행 방법](#-실행-방법)
- [트러블슈팅](#-트러블슈팅)

---

##  서비스 소개

**BillGates**는 통신사의 핵심 업무인 **빌링(Billing)**과 **고객 알림**을 처리하는 대용량 메시지 처리 시스템입니다.

### 주요 기능

- **대규모 정산 처리**: 100만 회원의 월 500만 건 사용 이력 데이터를 자동으로 정산
- **비동기 메시지 발송**: Kafka 기반으로 100만 건의 청구서를 이메일/SMS로 발송
- **안정적인 장애 처리**: 발송 실패 시 자동으로 대체 채널(SMS)로 전환
- **예약 발송**: 고객이 선택한 청구일에 맞춰 메시지 발송
- **금지 시간대 처리**: 고객이 설정한 수신 거부 시간대를 자동으로 회피

### 데이터 규모

- **회원 수**: 100만 명
- **월별 사용 이력**: 500만 건
- **월별 청구 발송**: 100만 건 (이메일/SMS)

---

##  프로젝트 배경

통신사의 핵심적이고 기초적인 업무인 **"빌링"**, **"고객 알림"**을 주제로 한 대용량 메시지 처리 아키텍처 프로젝트입니다.

### 문제 정의

통신사에서는 매달 다음과 같은 대규모 작업이 필요합니다:

- 수천만 건의 요금 청구서 생성
- 데이터 사용량 경고 알림
- 가입/해지 문자 발송

이러한 작업은 **안정성**과 **대용량 처리**가 필수이며, 백엔드 엔지니어로서 반드시 경험해야 할 핵심 기술입니다.

### 해결 방안

1. **Spring Batch**: 대용량 정산 처리 자동화
2. **Apache Kafka**: 비동기 메시지 처리로 시스템 부하 분산
3. **병렬 처리**: 12개 Partition을 활용한 고성능 메시지 발송

---

##  기술적 달성

### 1. 대용량 통신 요금 정산 시스템

#### Spring Batch를 활용한 정산 처리

- **100만 명의 회원**에 대한 **500만 건의 사용 이력** 데이터를 처리하여 월별 청구 금액을 자동으로 계산합니다.
- 매월 지정일에 자동 실행되며, **요금제, 통화료, 소액결제** 등 카테고리별로 금액을 집계하여 정산 결과 테이블에 저장합니다.

#### 성능 최적화

- **복합 인덱스** (`member_id`, `usage_date`)를 활용하여 조회 성능을 향상시킵니다.
- 카테고리별 합계를 미리 계산하여 청구서 조회 속도를 개선합니다.

#### 처리 흐름

```
USAGE_HISTORY (500만 건)
    ↓
Spring Batch (정산 계산)
    ↓
BILLING (100만 건)
    ↓
MESSAGE 생성 (100만 건)
```

---

### 2. Kafka 기반 청구 메시지 전송 시스템

#### 비동기 메시지 전송 구조

- **Apache Kafka**를 메시지 브로커로 사용하여 정산 시스템과 알림 시스템을 결합합니다.
- 정산 완료 후 청구 알림 메시지를 Kafka Topic에 전송하며, **Producer는 메시지를 전송만 하고 즉시 반환**되어 배치 실행 시간을 대폭 단축합니다.
- **채널별 Topic 분리** (`notification-email`, `notification-sms`)를 통해 독립적인 운영과 확장이 가능합니다.

#### 병렬 처리 아키텍처

```
Kafka Topic (12 Partitions)
    ↓
12개의 Consumer Thread (병렬 처리)
    ↓
초당 1,200건 처리
```

**성능 향상:**
- 단일 Thread: 100만 건 처리 시 약 2.7시간 소요
- 12개 Thread: 100만 건 처리 시 약 14분 소요
- **12배 성능 향상** 달성

#### 발송 실패 처리 및 중복 방지

- **1% 확률의 랜덤 발송 실패**를 시뮬레이션하며, 이메일 발송 실패 시 자동으로 SMS Topic에 메시지를 전송하여 **대체 채널**로 발송합니다.
- **메시지 전송 이력 테이블**을 통해 동일한 청구서에 대한 중복 발송을 방지하고, 각 메시지에 고유 식별자를 부여하여 중복 여부를 체크합니다.

#### 메시지 내용 및 모니터링

- 청구서에는 **회원 정보, 총 청구 금액, 카테고리별 상세 내역**이 포함되며, 로그를 통해 발송 내용을 출력합니다.
- **발송 성공률, 실패율, 평균 처리 시간** 등의 통계를 수집하고, 배치 실행 로그와 메시지 발송 로그를 통해 전체 프로세스를 추적합니다.

#### 데이터 흐름

```
Spring Batch 완료
    ↓
Kafka Producer (메시지 전송)
    ↓
Kafka Topic (12 Partitions)
    ↓
Consumer (12 Thread 병렬 처리)
    ↓
이메일/SMS 발송
    ↓
MESSAGE_SEND_HISTORY (결과 저장)
```

---

### 3. 예약 발송 및 금지 시간대 처리

#### 고객 맞춤 청구일 설정

- **정산일**과 **청구일**을 분리하여 고객이 원하는 날짜에 청구서를 받을 수 있도록 지원합니다.
- 고객별로 선호하는 청구일(매월 1일, 5일, 10일 등)을 설정하고, Spring Scheduler가 매일 해당 시간에 발송 대상을 조회하여 Kafka로 전송합니다.

#### 발송 금지 시간대 자동 회피

- 고객이 설정한 **수신 거부 시간대**(예: 밤 10시 ~ 아침 8시)를 확인하여 해당 시간대를 자동으로 회피합니다.
- 금지 시간대에 해당하는 메시지는 **다음 가능 시간으로 자동 연기**되어 고객 경험을 개선합니다.

#### 처리 흐름

```
매일 오전 9시 Scheduler 실행
    ↓
오늘 발송 대상 조회 (reserved_at = 오늘)
    ↓
금지 시간대 확인
    ↓
발송 가능 → Kafka 전송
발송 불가 → 다음 시간으로 연기
```

---

## 📆 프로젝트 일정

**2026.01.07 ~ 2026.01.27**

| 기간 | 단계 | 주요 내용 |
|------|------|-----------|
| **1/7 ~ 1/9** | 기획 및 설계 | 주제 선정 및 일정 수립, 세부 기획 구상 |
| **1/12 ~ 1/16** | 구현 (Core) | 더미데이터 적재, Spring Batch 정산 기능, Kafka 메시지 전송 |
| **1/19 ~ 1/23** | 구현 (Advanced) | 예약 발송, 금지 시간대, 실패 처리, 통계 |
| **1/26** | 마무리 | 발표 자료, 시연 영상, README 정리 |
| **1/27** | 발표 | 최종 제출 및 발표 |

---

## 🛠️ 기술 스택

### Backend
![Java](https://img.shields.io/badge/Java_17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_3.5.9-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Spring Batch](https://img.shields.io/badge/Spring_Batch-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Spring Data JPA](https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white)

### Message Queue
![Apache Kafka](https://img.shields.io/badge/Apache_Kafka_3.9.1-231F20?style=for-the-badge&logo=apache-kafka&logoColor=white)

### Database
![MySQL](https://img.shields.io/badge/MySQL_8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)

### DevOps
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Docker Compose](https://img.shields.io/badge/Docker_Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)

### Build Tool
![Gradle](https://img.shields.io/badge/Gradle_8.x-02303A?style=for-the-badge&logo=gradle&logoColor=white)

---


##  실행 방법

### 사전 준비

- Java 17
- Docker & Docker Compose
- MySQL 8.0
- Gradle

### 1. 환경 변수 설정

`.env` 파일 생성:

```properties
# MySQL
DB_URL=jdbc:mysql://localhost:3306/{DBNAME}?serverTimezone=Asia/Seoul
DB_USERNAME={USERNAME}
DB_PASSWORD={PASSWORD}

# Encryption
ENCRYPTION_SECRET=your-secret-key-32-characters!!
ENCRYPTION_IV=your-iv-16chars!
```

### 2. Kafka 실행 (Docker Compose)

```bash
docker-compose up -d
```

**Kafka UI 확인:**
```
http://localhost:8989
```

### 3. Spring Boot 실행

```bash
./gradlew bootRun
```

### 4. 테스트 API 호출

#### 이메일 단건 테스트
```bash
curl http://localhost:8080/api/test/email
```

#### SMS 단건 테스트
```bash
curl http://localhost:8080/api/test/sms
```

#### 대량 테스트 (100건)
```bash
curl "http://localhost:8080/api/test/bulk?count=100"
```

#### 통계 확인
```bash
curl http://localhost:8080/api/test/stats
```


---

##  핵심 성과

### 1. 고성능 달성
- **12개 Partition** 병렬 처리로 **초당 1,200건** 처리
- 100만 건 발송 시간을 **2.7시간 → 14분**으로 단축 (**12배 성능 향상**)

### 2. 안정적인 장애 처리
- 1% 실패 시뮬레이션 및 자동 SMS 대체 발송
- 메시지 전송 이력 관리로 **중복 발송 방지**
- 실시간 통계 수집 및 모니터링

### 3. 고객 맞춤 서비스
- **예약 발송**: 고객이 선택한 청구일에 자동 발송
- **금지 시간대 회피**: 수신 거부 시간대 자동 처리
- 채널별 독립 운영 (이메일/SMS)

---

##  트러블슈팅

### 1. Kafka 메타데이터 관리: KRaft vs ZooKeeper

#### 문제 상황
Kafka 초기 구성 시 메타데이터 관리 방식을 선택해야 했습니다.

#### 선택지 비교

| 항목 | ZooKeeper 방식 | KRaft 방식 |
|------|---------------|-----------|
| **구조** | Kafka + ZooKeeper 별도 운영 | Kafka 단독 운영 |
| **복잡도** | 높음 (2개 시스템 관리) | 낮음 (단일 시스템) |
| **성능** | ZooKeeper 병목 가능 | 더 빠른 메타데이터 처리 |
| **안정성** | 검증된 방식 (구버전) | Kafka 3.0+ 권장 방식 |

#### 선택 및 결과

**선택**: **KRaft 방식** 채택

**이유**:
- Kafka 3.9.1 버전에서 **KRaft가 Production Ready** 상태
- ZooKeeper 의존성 제거로 **시스템 복잡도 감소**
- **메타데이터 처리 성능 향상** (Controller 분산)
- Kafka 공식 문서에서 **KRaft 권장** (ZooKeeper는 향후 제거 예정)

**docker-compose.yml 설정**:
```yaml
environment:
  KAFKA_CFG_PROCESS_ROLES: controller,broker
  KAFKA_CFG_CONTROLLER_QUORUM_VOTERS: 0@kafka:9093
  KAFKA_CFG_CONTROLLER_LISTENER_NAMES: CONTROLLER
```

**결과**:
- 단일 컨테이너로 Kafka 운영 성공
- ZooKeeper 없이 안정적인 메타데이터 관리
- 향후 Kafka 업그레이드 시에도 호환성 유지

---

### 2. Docker Image 선택: Bitnami → Confluent Inc

#### 문제 상황
초기에는 Bitnami Kafka 이미지를 사용하려 했으나, 실제 운영 환경과의 호환성 문제가 발생했습니다.

#### 선택지 비교

| 항목 | Bitnami | Confluent Inc |
|------|---------|---------------|
| **이미지 크기** | 작음 | 큼 |
| **설정 방식** | 환경변수 (간단) | 환경변수 (상세) |
| **운영 환경** | 개발/테스트 적합 | **Production 적합** |
| **커뮤니티** | 중간 | **대규모 (공식)** |
| **문서화** | 보통 | **매우 상세** |

#### 발생한 문제

**Bitnami 사용 시**:
```yaml
# docker-compose.yml (Bitnami)
image: bitnami/kafka:latest

# 문제점:
# 1. KRaft 설정 관련 문서 부족
# 2. Listener 설정 오류 빈번
# 3. 프로덕션 환경 예시 부족
```

#### 선택 및 결과

**선택**: **Confluent Inc 이미지** 채택

**이유**:
- Confluent는 Kafka 창시자들이 만든 회사로 **공식 이미지 제공**
- **Production 환경 검증**된 설정 및 문서 제공
- **Kafka UI와의 호환성** 우수
- 상세한 설정 예시와 **트러블슈팅 가이드** 제공

**변경된 docker-compose.yml**:
```yaml
services:
  kafka:
    image: confluentinc/cp-kafka:latest  # Confluent 이미지
    environment:
      KAFKA_NODE_ID: 1
      KAFKA_PROCESS_ROLES: 'broker,controller'
      KAFKA_CONTROLLER_QUORUM_VOTERS: '1@kafka:29093'
      # ... (상세 설정)
```

**결과**:
- KRaft 모드 안정적 동작
- Listener 설정 명확하게 구성 가능
- Kafka UI 연동 문제 없음
- 공식 문서 참고로 빠른 문제 해결

---

### 3. Kafka Listener 설정: 단일 포트 → 이중 리스너

#### 문제 상황
초기에는 9092 포트 하나만 설정했으나, Spring Boot 애플리케이션에서 Kafka 연결이 실패했습니다.

#### 발생한 에러

**Spring Boot 로그**:
```
org.apache.kafka.common.errors.TimeoutException: 
Failed to update metadata after 60000 ms.
```

**docker-compose.yml (초기 설정)**:
```yaml
environment:
  KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
```


**문제점**:
- **외부(호스트)**에서 `localhost:9092` 접속 시도
- Kafka는 `localhost:9092`를 **컨테이너 내부 주소**로 인식
- 메타데이터 응답에 `localhost:9092` 반환 → **연결 불가**

#### 해결 방법: 이중 리스너 구성

**수정된 docker-compose.yml**:
```yaml
environment:
  # 리스너 2개 선언
  KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: 'CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT'
  
  # 외부 접속용 (localhost:9092)
  KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:29092,PLAINTEXT_HOST://localhost:9092
  
  # 내부 통신용 (kafka:29092)
  KAFKA_LISTENERS: PLAINTEXT://kafka:29092,PLAINTEXT_HOST://0.0.0.0:9092,CONTROLLER://kafka:29093
  
  # 내부 통신은 kafka:29092 사용
  KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT

ports:
  - "9092:9092"   # 외부 접속용
  - "29092:29092" # 내부 통신용 (실제로는 expose만 해도 됨)
```


**리스너 역할 분리**:

| 리스너 | 용도 | 접속 주소 | 사용처 |
|--------|------|-----------|--------|
| **PLAINTEXT_HOST** | 외부 접속 | `localhost:9092` | Spring Boot (호스트) |
| **PLAINTEXT** | 내부 통신 | `kafka:29092` | Kafka UI, 컨테이너 간 통신 |
| **CONTROLLER** | 메타데이터 관리 | `kafka:29093` | KRaft Controller |

#### 결과

**application.properties**:
```properties
spring.kafka.bootstrap-servers=localhost:9092  # 외부 접속
```

**Kafka UI (docker-compose.yml)**:
```yaml
kafka-ui:
  environment:
    KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS: kafka:29092  # 내부 통신
```

**성공 로그**:
```
Started BillGatesApplication in 3.5 seconds
Kafka connection successful: localhost:9092
Topic created: notification-email (12 partitions)
```

**개선 효과**:
- 호스트에서 Spring Boot 연결 성공
- Docker 내부에서 Kafka UI 연결 성공
- 네트워크 격리 및 보안 향상
- 실제 운영 환경 구조와 유사한 설정

---

##  향후 개발 계획

- [ ] DLQ (Dead Letter Queue) 구현
- [ ] 재시도 로직 고도화
- [ ] 실제 이메일/SMS API 연동
- [ ] 실시간 모니터링 대시보드
- [ ] 성능 테스트 자동화
- [ ] AWS 인프라 구축

---

##  라이센스

MIT License