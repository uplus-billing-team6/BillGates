-- 메시지 템플릿 body 수정

UPDATE MESSAGE_TEMPLATE
SET body =
'[수신: {recipient}]\n\n안녕하세요, 고객님.\n\nBillGates 서비스를 이용해 주셔서 감사합니다.\n{month} 이용 요금 명세서가 발행되었습니다.\n\n■ 청구 요약\n- 청구 월: {month}\n- 총 청구 금액: {totalAmount}원\n\n■ 상세 이용 내역\n{itemList}\n\n납부 기한 내에 납부 부탁드립니다.\n감사합니다.'
WHERE template_id = 1
  AND channel = 'EMAIL';

UPDATE MESSAGE_TEMPLATE
SET body =
'[수신: {recipient}]\n\n[BillGates] {month} 요금 안내\n고객님, 이번 달 청구 금액을 안내해 드립니다.\n\n■ 합계: {totalAmount}원\n\n■ 상세 내역\n{itemList}\n\n납부 기한 내 수납 바랍니다.'
WHERE template_id = 2
  AND channel = 'SMS';
