# BillGates - 통신 요금 정산 및 알림 시스템

## 목차

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

|                                                      김우식                                                       |                                                     이재혁                                                     |                                                        조성재                                                        |                                                           최보근                                                           |                                                      홍세민                                                       |
| :---------------------------------------------------------------------------------------------------------------: | :------------------------------------------------------------------------------------------------------------: | :------------------------------------------------------------------------------------------------------------------: | :------------------------------------------------------------------------------------------------------------------------: | :---------------------------------------------------------------------------------------------------------------: |
| [<img src="https://github.com/rladntlr.png" width="150" height="150"><br/>@rladntlr](https://github.com/rladntlr) | [<img src="https://github.com/ljh5918.png" width="150" height="150"><br/>@ljh5918](https://github.com/ljh5918) | [<img src="https://github.com/seongejae.png" width="150" height="150"><br/>@seongejae](https://github.com/seongejae) | [<img src="https://github.com/ChoiBoKeun1.png" width="150" height="150"><br/>@ChoiBoKeun1](https://github.com/ChoiBoKeun1) | [<img src="https://github.com/semsemin.png" width="150" height="150"><br/>@semsemin](https://github.com/semsemin) |
|                                                    **Backend**                                                    |                                             **Backend / Frontend**                                             |                                                     **Backend**                                                      |                                                        **Backend**                                                         |                                                    **Backend**                                                    |
|                                         역할 및 담당 기능<br>작성해주세요                                         |                                       역할 및 담당 기능<br>작성해주세요                                        |                                          역할 및 담당 기능<br>작성해주세요                                           |                                 더미데이터 생성 로직 구현 <br> Spring Batch 정산 로직 구현                                 |                                         역할 및 담당 기능<br>작성해주세요                                         |

<br>

## 서비스 소개

| 항목          | 내용                                                             |
| ------------- | ---------------------------------------------------------------- |
| **팀명**      | Billgates                                                        |
| **주제**      | 통신 빌링(Billing) 및 고객 알림을 위한 대용량 메시지 처리 시스템 |
| **타겟**      | 대규모 회원을 보유한 통신사·플랫폼 서비스                        |
| **개발 기간** | 2026.01.07 ~ 2026.01.27 (약 3주)                                 |

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

## 프로젝트 배경

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

| 기간            | 단계            | 주요 내용                                                  |
| --------------- | --------------- | ---------------------------------------------------------- |
| **1/7 ~ 1/9**   | 기획 및 설계    | 주제 선정 및 일정 수립, 세부 기획 구상                     |
| **1/12 ~ 1/16** | 구현 (Core)     | 더미데이터 적재, Spring Batch 정산 기능, Kafka 메시지 전송 |
| **1/19 ~ 1/23** | 구현 (Advanced) | 예약 발송, 금지 시간대, 실패 처리, 통계                    |
| **1/26**        | 마무리          | 발표 자료, 시연 영상, README 정리                          |
| **1/27**        | 발표            | 최종 제출 및 발표                                          |

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

<img width="2378" height="1362" alt="유레카 종합프로젝트 6조 (2)" src="https://private-user-images.githubusercontent.com/84448281/540392185-0bc8fdcc-cd5c-4e2b-876d-3e87e4d4abdd.png?jwt=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJnaXRodWIuY29tIiwiYXVkIjoicmF3LmdpdGh1YnVzZXJjb250ZW50LmNvbSIsImtleSI6ImtleTUiLCJleHAiOjE3Njk0MTM1NTksIm5iZiI6MTc2OTQxMzI1OSwicGF0aCI6Ii84NDQ0ODI4MS81NDAzOTIxODUtMGJjOGZkY2MtY2Q1Yy00ZTJiLTg3NmQtM2U4N2U0ZDRhYmRkLnBuZz9YLUFtei1BbGdvcml0aG09QVdTNC1ITUFDLVNIQTI1NiZYLUFtei1DcmVkZW50aWFsPUFLSUFWQ09EWUxTQTUzUFFLNFpBJTJGMjAyNjAxMjYlMkZ1cy1lYXN0LTElMkZzMyUyRmF3czRfcmVxdWVzdCZYLUFtei1EYXRlPTIwMjYwMTI2VDA3NDA1OVomWC1BbXotRXhwaXJlcz0zMDAmWC1BbXotU2lnbmF0dXJlPWU2Y2U5ZDA5NWZhYTZiNWZhN2E2NmFmODgyNjk0YzU4NjE1OTg3ZWVkOWE4NjgzYzI1NzczZDUzN2I3NzVjYzUmWC1BbXotU2lnbmVkSGVhZGVycz1ob3N0In0.GAqrq-oFujPApBqOl76H9PU7RHTXJL3kK_cDSeKDSLQ" />

<br>
<br>

## 기술적 달성

<details>
<summary><strong>1. 대용량 통신 요금 정산 시스템</strong></summary>

#### Spring Batch를 활용한 정산 처리

- **대규모 데이터 처리**: **100만 명의 회원**과 **500만 건의 사용 이력**을 처리하여 월별 청구서를 생성합니다
- **복합 정산 로직**: 단순 합산이 아닌, **요금제 기본료 + 통화료 + 소액결제**를 합산한 뒤, 회원별 **할인 정책(정액/정률)** 을 적용하여 최종 청구 금액을 확정합니다.
- **데이터 정합성 보장**: `Reader` → `Processor` → `Writer`의 트랜잭션 범위를 Chunk 단위로 관리하여 정산 데이터의 무결성을 보장합니다.

#### 성능 최적화

기존 JPA `saveAll`의 성능 한계를 극복하고 대용량 처리를 위해 다음과 같은 최적화를 수행했습니다.

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

#### 고객 맞춤 청구일 설정

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

<details>
<summary><strong>4. 대용량 데이터 적재 성능 최적화 (Batch Insert)</strong></summary>

#### 문제 정의

- **월 500만 건**의 정산 데이터를 적재하는 과정에서 JPA의 `IDENTITY` 전략 사용 시 **Bulk Insert가 비활성화**되는 문제가 발생했습니다.
- 단건 `INSERT` 발생으로 네트워크 왕복(Round-trip)이 500만 번 발생하여 전체 배치 성능이 저하되었습니다.

#### 해결 방안: JDBC Template 도입

- JPA의 영속성 컨텍스트 오버헤드를 제거하고 **Batch Insert를 강제**하기 위해 `JdbcTemplate`을 채택했습니다.
- `batchUpdate()` 메서드를 활용하여 데이터를 **Chunk 단위(1,000건)**로 묶어 전송함으로써 네트워크 I/O를 획기적으로 줄였습니다.

#### 결과

- 기존 JPA 대비 데이터 적재 속도 **약 10배 향상**
- 월간 500만 건 데이터의 안정적이고 빠른 처리 프로세스 구축

</details>

<br>

## 핵심 성과

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

## 트러블슈팅

<details>
<summary><strong> Kafka 메타데이터 관리: KRaft vs ZooKeeper</strong></summary>

#### 문제 상황

Kafka 초기 구성 시 메타데이터 관리 방식을 선택해야 했습니다.

#### 선택지 비교

| 항목       | ZooKeeper 방식              | KRaft 방식              |
| ---------- | --------------------------- | ----------------------- |
| **구조**   | Kafka + ZooKeeper 별도 운영 | Kafka 단독 운영         |
| **복잡도** | 높음 (2개 시스템 관리)      | 낮음 (단일 시스템)      |
| **성능**   | ZooKeeper 병목 가능         | 더 빠른 메타데이터 처리 |
| **안정성** | 검증된 방식 (구버전)        | Kafka 3.0+ 권장 방식    |

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

| 항목            | Bitnami          | Confluent Inc       |
| --------------- | ---------------- | ------------------- |
| **이미지 크기** | 작음             | 큼                  |
| **설정 방식**   | 환경변수 (간단)  | 환경변수 (상세)     |
| **운영 환경**   | 개발/테스트 적합 | **Production 적합** |
| **커뮤니티**    | 중간             | **대규모 (공식)**   |
| **문서화**      | 보통             | **매우 상세**       |

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
    image: confluentinc/cp-kafka:latest # Confluent 이미지
    environment:
      KAFKA_NODE_ID: 1
      KAFKA_PROCESS_ROLES: "broker,controller"
      KAFKA_CONTROLLER_QUORUM_VOTERS: "1@kafka:29093"
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
  KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: "CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT"

  # 외부 접속용 (localhost:9092)
  KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:29092,PLAINTEXT_HOST://localhost:9092

  # 내부 통신용 (kafka:29092)
  KAFKA_LISTENERS: PLAINTEXT://kafka:29092,PLAINTEXT_HOST://0.0.0.0:9092,CONTROLLER://kafka:29093

  # 내부 통신은 kafka:29092 사용
  KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT

ports:
  - "9092:9092" # 외부 접속용
  - "29092:29092" # 내부 통신용 (실제로는 expose만 해도 됨)
```

**리스너 역할 분리**:

| 리스너             | 용도            | 접속 주소        | 사용처                     |
| ------------------ | --------------- | ---------------- | -------------------------- |
| **PLAINTEXT_HOST** | 외부 접속       | `localhost:9092` | Spring Boot (호스트)       |
| **PLAINTEXT**      | 내부 통신       | `kafka:29092`    | Kafka UI, 컨테이너 간 통신 |
| **CONTROLLER**     | 메타데이터 관리 | `kafka:29093`    | KRaft Controller           |

#### 결과

**application.properties**:

```properties
spring.kafka.bootstrap-servers=localhost:9092  # 외부 접속
```

**Kafka UI (docker-compose.yml)**:

```yaml
kafka-ui:
  environment:
    KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS: kafka:29092 # 내부 통신
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

<details>
<summary><strong>서버 확장(Scale-out) 시 스케줄러 중복 실행 방지 (Redis Lock)</strong></summary>

#### 문제 상황

트래픽 분산을 위해 서버를 2대 이상으로 Scale-out 했을 때, 동일한 배치 작업이 모든 서버에서 동시에 실행되어 **정산 데이터 중복 생성** 및 **알림 이중 발송** 위험이 발생했습니다.

#### 원인 분석

Spring Boot의 기본 `@Scheduled`는 애플리케이션(JVM) 레벨에서 동작하므로, 여러 서버 간의 실행 상태를 동기화하는 **전역 잠금(Global Lock)** 장치가 부재했습니다.

#### 해결 시도

- **Redis 분산 락(Distributed Lock)** 도입: DB 락보다 오버헤드가 적은 Redis `SETNX`를 활용했습니다.
- **JobLockListener 구현**: 배치 실행 직전 락 획득을 시도하고, 실패 시 실행을 차단했습니다.
- **Lua Script 적용**: 락 해제 시 원자성(Atomicity)을 보장했습니다.

#### 최종 결과

서버가 N대로 늘어나더라도 락을 획득한 **단 하나의 서버에서만 배치가 실행**됨을 보장하여 데이터 무결성을 확보했습니다.

</details>

<details>
<summary><strong>서버 재시작 시 스케줄러 폭주 제어 (운영 안정성)</strong></summary>

#### 문제 상황

개발 중 서버 재시작 시 `@Scheduled`가 즉시 가동되어, DB에 잔존해 있던 테스트 데이터 수천 건이 일시에 처리되면서 **시스템 과부하**가 발생했습니다.

#### 해결 시도

- **환경별 스위칭(Switching)** 기능 도입: `@ConditionalOnProperty`를 사용하여 `application.properties` 설정 값에 따라 스케줄러 실행 여부를 제어했습니다.

#### 최종 결과

개발 환경에서는 불필요한 자동 실행을 방지하여 디버깅 효율을 높이고, 운영 환경에서는 의도치 않은 **사고를 방지하는 안전장치**를 마련했습니다.

</details>

<details>
<summary><strong>방해금지 시간(DnD) 무한 루프 방지 로직 개선</strong></summary>

#### 문제 상황

방해금지 시간대(22:00~08:00) 데이터를 단순히 `continue`로 건너뛰면, 스케줄러가 1초 뒤 동일 데이터를 다시 조회하는 **Polling 무한 루프**가 발생하여 DB CPU를 낭비했습니다.

#### 해결 시도

- **재스케줄링(Rescheduling)** 로직 적용: 발송 예약 시간을 "익일 08:00"로 업데이트하고, 상태를 `DEFERRED`로 변경하여 조회 대상에서 제외했습니다.

#### 최종 결과

불필요한 반복 조회를 제거하여 DB 부하를 줄이고, 사용자 지정 시간에 정확히 발송되는 순차 처리 로직을 완성했습니다.

</details>

<details>
<summary><strong>대량 조회 시 N+1 문제 해결 (Performance)</strong></summary>

#### 문제 상황

발송 대상 메시지 조회 시, 연관된 회원 정보(이메일 등)를 가져오기 위해 건마다 추가 쿼리가 발생하는 **N+1 문제**로 조회 성능이 저하되었습니다.

#### 해결 시도

- **JPQL Fetch Join** 적용: `MessageRepository`에서 메시지와 회원 정보를 단 **한 번의 쿼리**로 함께 로딩하도록 최적화했습니다.

#### 최종 결과

조회 쿼리를 단 1회로 고정하여 대용량 데이터 처리 시에도 안정적인 DB 성능을 확보했습니다.

</details>

<details>
<summary><strong> Kafka Partition 수 vs Consumer 동시성</strong></summary>
# Kafka Partition 수 vs Consumer 동시성

## 배경

Kafka 기반으로 100만 건의 이메일 알림을 발송하는 배치 작업에서 처리 속도가 예상보다 심각하게 느린 현상이 발생했습니다.

## 문제 상황

100만 건의 알림 메시지를 Kafka를 통해 발송할 때, 예상보다 처리 속도가 현저히 느린 현상이 발생했습니다.

- **예상 처리 시간**: 약 10분
- **실제 처리 시간**: 약 2.7시간

### 이상 징후 - Consumer를 늘렸는데 빨라지지 않는다

Consumer의 Concurrency 설정을 10으로 높였음에도 불구하고 성능 개선이 이루어지지 않았습니다.

```java
// KafkaConsumerConfig.java
factory.setConcurrency(10);  // 동시에 10개 스레드로 처리하도록 설정
```

CPU, 메모리 모두 여유가 있었고 스레드도 정상적으로 생성되어 보였습니다.

## 원인 분석

Kafka의 Partition과 Consumer 관계에 대한 이해 부족이 원인이었습니다.

### Kafka의 핵심 제약

> **"하나의 Partition은 동일 Consumer Group 내에서 오직 하나의 Consumer만 읽을 수 있다"**

이 핵심 제약을 간과하고 있었습니다.

### 초기 설정 상태

```java
// KafkaConfig.java - Topic 생성
@Bean
public NewTopic emailTopic() {
    return TopicBuilder.name("notification-email")
            .partitions(3)      // 파티션 3개로 생성
            .replicas(1)
            .build();
}

// KafkaConsumerConfig.java
factory.setConcurrency(10);  // Consumer 10개로 설정
```

### 실제 동작 상황

```
Topic: notification-email (Partition 3개)

  Partition 0  ──→  Consumer 0  ✅ 메시지 처리 중
  Partition 1  ──→  Consumer 1  ✅ 메시지 처리 중
  Partition 2  ──→  Consumer 2  ✅ 메시지 처리 중
                    Consumer 3  ❌ Idle (놀고 있음)
                    Consumer 4  ❌ Idle (놀고 있음)
                    Consumer 5  ❌ Idle (놀고 있음)
                    Consumer 6  ❌ Idle (놀고 있음)
                    Consumer 7  ❌ Idle (놀고 있음)
                    Consumer 8  ❌ Idle (놀고 있음)
                    Consumer 9  ❌ Idle (놀고 있음)

→ Concurrency를 10으로 설정했지만 실제로는 3개 Consumer만 동작
```

## 해결 과정

### Partition 증가

#### 1차 시도: Partition 3개 → 6개로 증가

```java
// KafkaConfig.java
@Bean
public NewTopic emailTopic() {
    return TopicBuilder.name("notification-email")
            .partitions(6)      // 3 → 6개로 증가
            .replicas(1)
            .build();
}

// KafkaConsumerConfig.java
factory.setConcurrency(6);   // 파티션 수에 맞춰 조정
```

**결과**: 처리 시간: 2.7시간 → 약 50분 (약 3배 성능 향상)

#### 2차 시도: Partition 6개 → 12개로 증가

```java
// KafkaConfig.java
@Bean
public NewTopic emailTopic() {
    return TopicBuilder.name("notification-email")
            .partitions(12)     // 6 → 12개로 증가
            .replicas(1)
            .build();
}

// KafkaConsumerConfig.java
factory.setConcurrency(10);  // DB Connection Pool(100개) 고려하여 10으로 제한
```

**결과**: 처리 시간: 50분 → 약 14분 (추가 약 3배 성능 향상)

**최종 성능**: 초기 대비 약 12배 향상

### 주의사항: Partition은 늘릴 수만 있고 줄일 수 없다

| 작업        | 가능 여부 |
| ----------- | --------- |
| 파티션 증가 | ✅ 가능   |
| 파티션 감소 | ❌ 불가능 |

### 줄일 수 없는 이유

Kafka는 Partition 내에서 메시지의 순서를 보장합니다. Partition 수를 줄이면 기존 Partition에 저장된 메시지들의 재배치가 필요한데, 이 과정에서 메시지 순서가 깨질 수 있습니다. Kafka는 데이터 무결성 문제를 원천적으로 방지하기 위해 Partition 감소를 허용하지 않습니다.

### Partition을 줄여야 하는 경우 대안

1. 원하는 Partition 수로 새로운 Topic 생성
2. 기존 Topic의 데이터를 새 Topic으로 마이그레이션
3. Consumer가 새 Topic을 바라보도록 설정 변경
4. 기존 Topic 삭제

### Partition 설계 권장 전략

```
초기 설계 → 보수적으로 시작 (3~6개)
         ↓
      모니터링 및 성능 측정
         ↓
필요시   → 점진적 증가 (6 → 12 → 24)
         ↓
주의     → 과도하게 늘리지 않기 (되돌릴 수 없음)
```

## 결과

### 성능 개선 요약

| 단계     | Partition | Concurrency | 처리 시간 | 초당 처리량 |
| -------- | --------- | ----------- | --------- | ----------- |
| 초기     | 3         | 10 (실제 3) | 2.7시간   | ~100건      |
| 1차 개선 | 6         | 6           | 50분      | ~330건      |
| 2차 개선 | 12        | 10          | 14분      | ~1,200건    |

**최종 성능**: 초기 대비 약 12배 향상

## 핵심 교훈

1. **Concurrency ≤ Partition 수**: Partition보다 Consumer를 많이 만들어도 의미가 없습니다. Concurrency를 Partition 수보다 높게 설정해도 실제로는 Partition 수만큼만 동작합니다.
2. **Partition 설계의 중요성**: 한번 늘린 Partition은 줄일 수 없으므로 초기 설계가 중요합니다. 보수적으로 시작하여 점진적으로 증가시켜야 합니다.
3. **리소스 간 균형**: Consumer 수를 늘릴 때 DB Connection Pool 크기도 함께 고려해야 합니다.
4. **점진적 확장**: 모니터링을 통해 필요시 Partition을 점진적으로 증가시키는 전략이 효과적입니다.
</details>
<details>
<summary><strong> DB Connection Pool 고갈 문제</strong></summary>

## 배경

Partition 개선으로 성능은 좋아졌지만, 일정 시점에서 항상 에러가 발생하며 배치가 중단되는 현상이 발생했습니다.

## 문제 상황

이메일 대량 발송 중 다음과 같은 에러가 발생하며 시스템이 멈추는 현상이 발생했습니다.

```
HikariPool-1 - Connection is not available, request timed out after 30000ms.
java.sql.SQLTransientConnectionException: HikariPool-1 - Connection is not available
```

100만 건 발송 작업 중 약 30% 지점(30만 건 처리 후)에서 DB Connection Pool이 고갈되어 전체 시스템이 멈추는 현상이 반복되었습니다.

### 이상한 점

- Consumer 동시성: 10
- DB Connection Pool: 100

→ 그런데 왜 고갈?

## 원인 분석

### 결정적 원인 - 트랜잭션 안에서 SMTP 통신

DB Connection을 점유한 상태에서 SMTP 통신(이메일 발송)을 수행하고 있었습니다.

### 기존 코드의 문제

```java
@Transactional  // ← 메서드 시작 시 DB Connection 획득
public List<Long> sendBatch(List<NotificationEvent> events) {

    for (NotificationEvent event : events) {
        // 1. 템플릿 조회 (DB 사용) - 빠름
        TemplateDto template = templateProvider.getTemplateById(TEMPLATE_ID);

        // 2. 이메일 발송 (SMTP 통신, 평균 300~500ms) - 느림! 여기가 문제!
        mailSender.send(message);

        // 3. 이력 저장 (DB 사용) - 빠름
        saveHistory(event.getMessageId(), true, ...);
    }

    return successIds;
}  // ← 메서드 종료 시 DB Connection 반환
```

문제는 DB Connection을 점유한 상태로 외부 SMTP 통신을 기다리고 있다는 점이었습니다.

### 문제 발생 메커니즘

```
┌─────────────────────────────────────────────────────────────┐
│  DB Connection Pool: 100개                                  │
│                                                             │
│  Consumer 1~10: 각각 Connection 점유 중                      │
│    └─ SMTP 발송 대기... (15초)                              │
│                                                             │
│  새로운 요청 → Connection 없음 → 타임아웃!                   │
└─────────────────────────────────────────────────────────────┘
```

### 수치로 보는 문제

- **배치 1회(50건) × 이메일 발송(300ms) = 약 15초 동안 Connection 점유**
- **Consumer 10개 → 동시에 10개 Connection이 각각 15초씩 점유**
- **새로운 DB 요청 → Connection 부족 → 30초 대기 → 타임아웃**

## 해결 과정

### 핵심 전략: DB 트랜잭션과 외부 I/O 완전 분리

- @Transactional 범위 내에서는 DB 작업만 수행
- 이메일 발송은 별도 ExecutorService에서 비동기 처리

`@Transactional` 범위 내에서는 DB 작업만 수행하고, SMTP 통신은 트랜잭션이 끝난 후 별도 스레드에서 비동기로 처리합니다.

### 개선된 코드

```java
// EmailMessageSender.java

// 비동기 이메일 발송용 스레드 풀 (5개 고정)
private final ExecutorService emailExecutor = Executors.newFixedThreadPool(5);

@Transactional
public List<Long> sendBatch(List<NotificationEvent> events) {
    List<Long> successIds = new ArrayList<>();

    // 발송할 이메일 정보를 임시 저장 (아직 발송하지 않음)
    List<EmailToSend> emailsToSend = new ArrayList<>();

    for (NotificationEvent event : events) {
        // 템플릿 조회 및 메시지 생성
        String finalTitle = messageFormatter.formatTitle(template, event.getEmailTitle());
        String finalBody = messageFormatter.formatBody(template, event.getContent());

        // 발송 대상만 리스트에 수집 (실제 발송은 나중에)
        emailsToSend.add(new EmailToSend(
            event.getRecipient(), finalTitle, finalBody
        ));

        successIds.add(event.getMessageId());
    }

    // 1. DB 작업: 이력 일괄 저장 (Bulk Insert)
    jdbcTemplate.batchUpdate(
        "INSERT IGNORE INTO MESSAGE_SEND_HISTORY ...",
        historyArgs
    );

    // 2. 실제 이메일 발송 (비동기 - 트랜잭션 종료 후 별도 스레드에서 처리)
    if (!emailsToSend.isEmpty()) {
        emailExecutor.submit(() -> {        // 별도 스레드 풀에서 실행
            for (EmailToSend email : emailsToSend) {
                sendRealEmail(email);       // DB Connection 없이 SMTP만 처리
            }
        });
    }

    return successIds;
}  // @Transactional 종료, DB Connection 즉시 반환!
```

### 개선된 흐름

```
┌─────────────────────────────────────────────────────────────┐
│  Consumer Thread (DB Connection 사용)                       │
│                                                             │
│  1. 템플릿 조회                         10ms                │
│  2. 발송 대상 수집                       5ms                │
│  3. 이력 Bulk Insert                   30ms                │
│  4. emailExecutor.submit() ──┐          1ms                │
│  5. Connection 반환           │                             │
│                              │                             │
│  총 점유 시간: ~46ms         │                             │
└──────────────────────────────│─────────────────────────────┘
                               ▼
┌─────────────────────────────────────────────────────────────┐
│  Email Executor Thread (DB Connection 미사용)               │
│                                                             │
│  → SMTP 통신만 수행                                         │
│  → DB Connection과 완전히 독립적                            │
└─────────────────────────────────────────────────────────────┘
```

### ExecutorService 선택

| 종류                      | 특징                    | 선택 여부      |
| ------------------------- | ----------------------- | -------------- |
| newFixedThreadPool(5)     | 고정 5개 스레드, 안정적 | ✅ 선택        |
| newCachedThreadPool()     | 필요시 스레드 무한 생성 | ❌ 메모리 위험 |
| newSingleThreadExecutor() | 단일 스레드 순차 처리   | ❌ 느림        |

## 결과

### 성능 개선 요약

| 항목                    | Before        | After             |
| ----------------------- | ------------- | ----------------- |
| DB Connection 점유 시간 | 15초/배치     | 46ms/배치         |
| Connection Pool 사용률  | 100% (고갈)   | 10~20%            |
| 배치 성공률             | 30%에서 중단  | 100% 완료         |
| 이메일 발송 방식        | 동기 (블로킹) | 비동기 (논블로킹) |

## 핵심 교훈

1. **외부 I/O는 트랜잭션 밖에서**: SMTP, HTTP 등 외부 통신은 DB 트랜잭션과 완전히 분리해야 합니다. DB Connection을 점유한 상태로 외부 통신을 기다리면 리소스 고갈이 발생합니다.
2. **@Transactional 범위 최소화**: 트랜잭션 내에서는 순수 DB 작업만 수행해야 합니다. DB Connection 점유 시간을 최소화하는 것이 핵심입니다.
3. **비동기 처리로 리소스 효율화**: ExecutorService를 활용하여 DB Connection 점유 시간을 단축하고, 외부 I/O는 독립적으로 처리합니다.
4. **Connection Pool 증설은 근본 해결책이 아니다**: Connection Pool 크기만 늘리는 것은 임시방편일 뿐, 아키텍처 개선이 필요합니다.
5. **장애는 수치로 증명해야 끝난다**: Before/After 측정을 통해 개선 효과를 명확히 검증해야 합니다.
</details>
<br>

## 실행 방법

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

## 향후 개발 계획

- [ ] DLQ (Dead Letter Queue) 구현
- [ ] 재시도 로직 고도화
- [ ] 실제 이메일/SMS API 연동
- [ ] 실시간 모니터링 대시보드
- [ ] 성능 테스트 자동화
- [ ] AWS 인프라 구축

---

Team Billgates | LG U+ URECA 백엔드 개발자 과정 3기 종합프로젝트 6조
