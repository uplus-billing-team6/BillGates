# BillGates - 통신 요금 정산 및 알림 시스템

##  목차
- [팀원 구성 및 역할 분담](#-팀원-구성-및-역할-분담)
- [서비스 소개](#-서비스-소개)
- [프로젝트 배경](#-프로젝트-배경)
- [프로젝트 일정](#-프로젝트-일정)
- [기술 스택](#️-기술-스택)
- [ERD](#-ERD)
- [기술적 달성](#-기술적-달성)
- [트러블슈팅](#-트러블슈팅)
- [실행 방법](#-실행-방법)


<br>

## 팀원 구성 및 역할 분담

### 👨‍💻 Team Billgates
**LG U+ URECA 백엔드 개발자 과정 3기 종합프로젝트 6조**

<br>

| 김우식 | 이재혁 | 조성재 | 최보근 | 홍세민 |
| :---: | :---: | :---: | :---: | :---: |
| [<img src="https://github.com/rladntlr.png" width="150" height="150"><br/>@rladntlr](https://github.com/rladntlr) | [<img src="https://github.com/ljh5918.png" width="150" height="150"><br/>@ljh5918](https://github.com/ljh5918) | [<img src="https://github.com/seongejae.png" width="150" height="150"><br/>@seongejae](https://github.com/seongejae) | [<img src="https://github.com/ChoiBoKeun1.png" width="150" height="150"><br/>@ChoiBoKeun1](https://github.com/ChoiBoKeun1) | [<img src="https://github.com/semsemin.png" width="150" height="150"><br/>@semsemin](https://github.com/semsemin) |
| **Backend** | **Backend / Frontend** | **Backend** | **Backend** | **Backend** |
| 역할 및 담당 기능<br>작성해주세요 | 역할 및 담당 기능<br>작성해주세요 | 역할 및 담당 기능<br>작성해주세요 | 더미데이터 생성 로직 구현 <br> Spring Batch 정산 로직 구현 | 역할 및 담당 기능<br>작성해주세요 |

<br>



##  서비스 소개

| 항목 | 내용 |
| --- | --- |
| **팀명** | Billgates |
| **주제** | 통신 빌링(Billing) 및 고객 알림을 위한 대용량 메시지 처리 시스템 |
| **타겟** | 대규모 회원을 보유한 통신사·플랫폼 서비스 |
| **개발 기간** | 2026.01.07 ~ 2026.01.27 (약 3주) |

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

<br>

##  프로젝트 배경

### 문제 정의

매달 다음과 같은 대규모 작업이 필요합니다:

- 수천만 건의 요금 청구서 생성
- 데이터 사용량 경고 알림
- 가입/해지 문자 발송

### 해결 방안

1. **Spring Batch**: 대용량 정산 처리 자동화
2. **Apache Kafka**: 비동기 메시지 처리로 시스템 부하 분산
3. **병렬 처리**: 12개 Partition을 활용한 고성능 메시지 발송

<br>

## 📆 프로젝트 일정

**2026.01.07 ~ 2026.01.27**

| 기간 | 단계 | 주요 내용 |
|------|------|-----------|
| **1/7 ~ 1/9** | 기획 및 설계 | 주제 선정 및 일정 수립, 세부 기획 구상 |
| **1/12 ~ 1/16** | 구현 (Core) | 더미데이터 적재, Spring Batch 정산 기능, Kafka 메시지 전송 |
| **1/19 ~ 1/23** | 구현 (Advanced) | 예약 발송, 금지 시간대, 실패 처리, 통계 |
| **1/26** | 마무리 | 발표 자료, 시연 영상, README 정리 |
| **1/27** | 발표 | 최종 제출 및 발표 |

### 🔗 [WBS](https://docs.google.com/spreadsheets/d/1SjeskgJBgr_TXsPWl35sV_5r3AC7aNji/edit?gid=124609301#gid=124609301)


<br>


## 🛠️ 기술 스택

### Backend
![Java](https://img.shields.io/badge/Java_17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_3.5.9-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Spring Batch](https://img.shields.io/badge/Spring_Batch-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Spring Data JPA](https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white)

### Message Queue
![Apache Kafka](https://img.shields.io/badge/Apache_Kafka_3.9.1-231F20?style=for-the-badge&logo=apache-kafka&logoColor=white)

### Database & Batch Control
![MySQL](https://img.shields.io/badge/MySQL_8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Flyway](https://img.shields.io/badge/Flyway-CC0200?style=for-the-badge&logo=flyway&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)

### API Docs
![Swagger](https://img.shields.io/badge/Swagger_2.8-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)


### DevOps
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Docker Compose](https://img.shields.io/badge/Docker_Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)

### Build Tool
![Gradle](https://img.shields.io/badge/Gradle_8.x-02303A?style=for-the-badge&logo=gradle&logoColor=white)

### Collaboration Tools
![Jira](https://img.shields.io/badge/Jira-0052CC?style=for-the-badge&logo=jira&logoColor=white)
![Slack](https://img.shields.io/badge/Slack-4A154B?style=for-the-badge&logo=slack&logoColor=white)
![Notion](https://img.shields.io/badge/Notion-000000?style=for-the-badge&logo=notion&logoColor=white)



<br>

## ERD
<img width="2378" height="1362" alt="유레카 종합프로젝트 6조 (2)" src="https://github.com/user-attachments/assets/65707240-6164-4252-8564-e2174ee2b8ea" />


<br>
<br>


##  기술적 달성

<details>
<summary><strong>1. 대용량 통신 요금 정산 시스템</strong></summary>

#### Spring Batch를 활용한 정산 처리

- **대규모 데이터 처리**: **100만 명의 회원**과 **500만 건의 사용 이력**을 처리하여 월별 청구서를 생성합니다
- **복합 정산 로직**: 단순 합산이 아닌, **요금제 기본료 + 통화료 + 소액결제**를 합산한 뒤, 회원별 **할인 정책(정액/정률)** 을 적용하여 최종 청구 금액을 확정합니다.
- **데이터 정합성 보장**: `Reader` → `Processor` → `Writer`의 트랜잭션 범위를 Chunk 단위로 관리하여 정산 데이터의 무결성을 보장합니다.

#### 성능 최적화
기존 JPA ```saveAll```의 성능 한계를 극복하고 대용량 처리를 위해 다음과 같은 최적화를 수행했습니다.
- **TSID 기반 Bulk Insert**
    - **문제**: DB Auto Increment 사용 시 Key 반환을 위한 Network Round-trip으로 인해 Bulk Insert가 불가능했습니다.
    - **해결**: ID 생성 주체를 DB에서 **Java(TSID)** 로 이관, ID를 Java app 에서 선채번(Pre-allocation)하고, `JdbcTemplate.batchUpdate`를 사용하여 **수천 건을 한 번에 Insert**했습니다.
    - **결과**: 처리 속도 **67% 단축** (테스트 결과 : 기존 15분 → **5분**)
- **N+1 문제 해결 (Reader 최적화)**
    - 회원 조회 시 연관된 '할인 정책'을 개별 조회하지 않고, **서브 쿼리**를 통해 해당 Chunk에 속한 회원의 할인 정보를 **한 번에 로딩(In-Memory Map)** 하여 쿼리 수를 획기적으로 줄였습니다.

#### 처리 흐름
```
[Reader]
• Member + Usage 조회
• Discount 정보 Fetch Join
      ↓
[Processor]
1. 사용량 합산
2. 할인 정책(Fixed/Percent) 적용
3. 최종 금액 계산
      ↓
[Writer]
1. TSID 채번 (Java)
2. Bulk Insert (4개 테이블 동시 저장)
```

#### 운영 및 안정성
- **스케줄링 및 수동 실행**
    - **자동**: 매월 1일 오전 04:00에 `BillingScheduler`가 자동으로 배치를 트리거합니다.
    - **수동**: `/api/batch/billing/run` API를 통해 실패한 월의 정산을 관리자가 즉시 재실행할 수 있습니다.
- **분산 락 (Distributed Lock)**
    - **목적**: 다중 서버 환경이나 스케줄러/API 중복 호출 시 배치가 **이중으로 실행되는 것을 방지**합니다.
    - **구현**: Redis `setIfAbsent`를 활용하여 Job Parameter(날짜)를 Key로 분산 락을 점유합니다. 이미 실행 중일 경우 **즉시 예외를 발생시켜(Fail-fast)** 중복 실행을 원천 차단합니다. (API 호출 시 `409 Conflict` 응답)
- **장애 복구 (Force Restart)**
    - 서버 비정상 종료로 배치가 `STARTED` 상태에 고립될 경우, `force=true` 파라미터를 통해 락을 강제 해제하고 프로세스를 안전하게 재시작할 수 있는 복구 로직을 구현했습니다.

<br>
</details>

<details>
<summary><strong>2. Kafka 기반 청구 메시지 전송 시스템</strong></summary>

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

<br>
</details>

<details>
<summary><strong>3. 지능형 발송 스케줄링 (예약 & 금지 시간대)</strong></summary>

### 🎯 Challenge & Solution

배치 완료 직후 일괄 발송 시 발생할 수 있는 '새벽 시간대 알림' 문제를 방지하고, 운영 효율성을 위해 **시스템 예약**과 **회원별 금지 시간대(DND)**를 결합한 하이브리드 스케줄링을 구현했습니다.

#### 1. 발송 시간 선계산 로직 (In-Batch Logic)
정산 배치(Processor) 단계에서 **시스템 전역 설정**과 **회원별 설정**을 조합하여 최종 발송 시각(`reserved_at`)을 미리 계산합니다.

1.  **시스템 일괄 예약 확인 (System Global Setting)**:
    - 시스템 설정 테이블(`RESERVATION_SETTING`)의 `is_reservation_active` 값을 확인합니다.
    - `TRUE`: 시스템이 지정한 일괄 발송 시간(예: 09:00)을 기준 시간으로 설정
    - `FALSE`: 배치가 완료되는 현재 시각(`NOW`)을 기준 시간으로 설정
2.  **회원별 금지 시간대 보정 (Member Personal Setting)**:
    - 회원의 `use_dnd` 컬럼을 확인하여 금지 시간대 사용 여부를 체크합니다.
    - **Time Shift**: 만약 기준 시간이 회원의 금지 시간대(예: 22:00 ~ 08:00)에 포함된다면, **금지 해제 직후(08:00:01)로 시간을 자동 보정(Shift)** 합니다.

#### 2. 처리 결과 저장
- 위 로직을 통해 확정된 `reserved_at`은 `MESSAGE` 테이블에 저장됩니다.
- 이후 발송기는 복잡한 시간 계산 없이, 단순히 **`reserved_at <= NOW`** 인 메시지만 조회하여 발송하므로 대용량 환경에서도 조회 성능이 극대화됩니다.

#### 3. 동적 예약 관리 (Admin API)
- 코드 수정이나 재배포 없이, **API를 통해 DB에 저장된 예약 설정을 실시간으로 변경**할 수 있습니다.
- 운영자는 상황에 따라 '즉시 발송 모드'와 '예약 발송 모드'를 자유롭게 전환할 수 있어 운영 효율성을 높였습니다.
    - `PUT /api/admin/reservation`: 예약 사용 여부(`true/false`) 및 발송 시각(`HH:mm`) 설정


</details>

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

<br>

##  트러블슈팅

<details>
<summary><strong> Kafka 메타데이터 관리: KRaft vs ZooKeeper</strong></summary>


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


</details>
<details>
<summary><strong> Docker Image 선택: Bitnami → Confluent Inc </strong></summary>

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

</details>
<details>

<summary><strong> Kafka Listener 설정: 단일 포트 → 이중 리스너 </strong></summary>

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

</details>


<br>

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

<br>

##  향후 개발 계획

- [ ] DLQ (Dead Letter Queue) 구현
- [ ] 재시도 로직 고도화
- [ ] 실제 이메일/SMS API 연동
- [ ] 실시간 모니터링 대시보드
- [ ] 성능 테스트 자동화
- [ ] AWS 인프라 구축

---

Team Billgates | LG U+ URECA 백엔드 개발자 과정 3기 종합프로젝트 6조
