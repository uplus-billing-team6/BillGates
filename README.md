# BillGates - 통신 요금 정산 및 알림 시스템

대용량 통신 요금 명세서 및 알림 발송 시스템

## 📋 목차

- [프로젝트 개요](#프로젝트-개요)
- [기술 스택](#기술-스택)
- [사전 준비](#사전-준비)
- [환경 설정](#환경-설정)
- [실행 방법](#실행-방법)
- [Kafka 테스트](#kafka-테스트)
- [API 명세](#api-명세)

---

## 사전 준비
**Docker & Docker Compose**
```bash
docker desktop

docker-compose -v.2

```
---

## 실행 방법

### 1. Kafka 환경 실행 (Docker Compose)

**프로젝트 루트에서 실행:**

```bash
docker-compose up -d
```

**실행 확인:**
```bash
docker ps
```

**Kafka UI 접속:**
```
http://localhost:8989
```

---

### 2. Kafka Topic 생성 확인

**Kafka UI에서 확인:**
```
1. http://localhost:8989 접속
2. Topics 메뉴 클릭
3. 아래 Topic 확인:
   - notification-email (12 partitions)
   - notification-sms (12 partitions)
```

**또는 CLI로 확인:**
```bash
docker exec -it kafka kafka-topics --list --bootstrap-server localhost:9092
```

---

## Kafka 테스트

### 테스트 API 목록

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/test/email` | 이메일 단건 테스트 |
| GET | `/api/test/sms` | SMS 단건 테스트 |
| GET | `/api/test/bulk?count={n}` | 대량 발송 테스트 |

---

### 1. 단건 테스트

#### 이메일 발송

**요청:**
```bash
curl http://localhost:8080/api/test/email
```

**또는 브라우저:**
```
http://localhost:8080/api/test/email
```

**응답:**
```json
{
  "status": "success",
  "message": "이메일 발송 요청 완료",
  "messageId": 1
}
```

**콘솔 로그 확인:**
```
[PRODUCER] 이메일 발송 요청: messageId=1
[CONSUMER] [EMAIL] message received
messageId=1
recipient=test@example.com
title=[LG U+] 12월 요금 안내
[EMAIL] kafka consume test SUCCESS
```

---

#### SMS 발송

**요청:**
```bash
curl http://localhost:8080/api/test/sms
```

**응답:**
```json
{
  "status": "success",
  "message": "SMS 발송 요청 완료",
  "messageId": 2
}
```

---

### 2. 소량 테스트 (10건)

**요청:**
```bash
curl http://localhost:8080/api/test/bulk?count=10
```

**응답:**
```json
{
  "status": "success",
  "message": "10건 발송 요청 완료",
  "count": 10,
  "elapsedTimeMs": 15
}
```

**콘솔 로그 확인 (병렬 처리):**
```
[ntainer#0-0-C-1] messageId=1
[ntainer#0-1-C-1] messageId=2
[ntainer#0-2-C-1] messageId=3
[ntainer#0-3-C-1] messageId=4
[ntainer#0-4-C-1] messageId=5
...
```

**주의:** 여러 Thread가 동시 처리하므로 로그 순서는 다를 수 있습니다.

---

### 3. 대량 테스트 (100건)

**요청:**
```bash
curl "http://localhost:8080/api/test/bulk?count=100"
```

**응답:**
```json
{
  "status": "success",
  "message": "100건 발송 요청 완료",
  "count": 100,
  "elapsedTimeMs": 85
}
```

---


### 4. Kafka UI에서 메시지 확인

**접속:**
```
http://localhost:8989
```

**확인 순서:**
```
1. Topics 메뉴 클릭
2. notification-email 클릭
3. Messages 탭 클릭
4. 발송된 메시지 확인
```

**메시지 내용 예시:**
```json
{
  "messageId": 1,
  "memberId": 100,
  "billingId": 200,
  "channel": "EMAIL",
  "recipient": "test@example.com",
  "emailTitle": "[LG U+] 12월 요금 안내",
  "content": "안녕하세요, 12월 요금은 89,000원입니다."
}
```

---

## 라이센스

MIT License

