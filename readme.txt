# [BillGates] 대용량 통신 요금 명세서 및 알림 발송 시스템 - 배치 개발 일지

## 1. 개요
- 작업 일자: 2026년 01월 12일
- 작업자: 조성재
- 작업 목표: 청구서 생성 배치(BillingJob)의 Job/Step 구조 설계 및 핵심 로직 구현
- 상태: 완료 (구조 설계 + DTO/Entity 구현 + 로직 최적화 반영)

## 2. 주요 구현 내용

### A. 패키지 및 클래스 구조
- springboot.billgates.batch.billing
  └─ BillingJobConfig.java : 배치 Job/Step 구성 (Chunk 지향 처리)
  └─ BillingItemDto.java   : Reader에서 DB 조회 결과를 담을 DTO
- springboot.billgates.domain
  └─ member.Member.java    : 회원 엔티티 (DB 스키마 반영)
  └─ billing.Billing.java  : 청구서 엔티티 (DB 스키마 반영)

### B. 배치 로직 설계 (BillingJob)
1. Reader (JdbcCursorItemReader)
   - MEMBER 테이블과 USAGE_HISTORY 테이블을 LEFT JOIN
   - JobParameter로 받은 월(month)을 기준으로 사용 기간(usage_date) 필터링
   - DB 레벨에서 SUM(amount)를 수행하여 애플리케이션 연산 부하 최소화
   - Paging 대신 Cursor 방식을 사용하여 대용량 데이터 조회 시 메모리 효율 확보

2. Processor (ItemProcessor)
   - Reader에서 집계된 금액(sumAmount)을 Billing 엔티티로 변환
   - (TODO) 추후 암호화된 전화번호/이메일 복호화 로직 추가 예정

3. Writer (JdbcBatchItemWriter)
   - Bulk Insert 적용 (JDBC Batch)
   - 멱등성(Idempotency) 확보: `ON DUPLICATE KEY UPDATE` 구문 적용
     -> 배치를 재실행하더라도 중복 에러 없이 기존 내역을 갱신하도록 처리
