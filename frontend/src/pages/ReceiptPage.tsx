/* eslint-disable @typescript-eslint/no-explicit-any */

// 영수증 테스트용 페이지 
// src/pages/ReceiptPage.tsx
// 단일 파일 버전: 타입 + 매핑 + 빌더 + UI + 인쇄 버튼까지 모두 포함.
// 라우터 없이도 바로 동작(모의 데이터 자동 주입)

import React, { useMemo } from "react";

/* =========================
   1) 타입 정의
   ========================= */
type SessionTurn = {
  sequenceNumber: number;
  question: string;
  selectedOptionId: number;
  selectedOptionText: string;
  correct: boolean;
  timestamp: string;
};

type LineItem = {
  sku: string;
  name: string;
  qty: number;
  unitPrice: number;
  total: number;
};

type Receipt = {
  receiptId: string;
  store: { name: string; address?: string };
  scenarioId: number;
  sessionId: string;
  startedAt: string;
  finishedAt: string;
  items: LineItem[];
  subtotal: number;
  tax: number;
  discount: number;
  total: number;
  payment: { method: "CASH" | "CARD" | "VIRTUAL"; masked?: string };
};

/* =========================
   2) 메뉴 매핑 (임시 키워드 매핑)
   - 선택지 텍스트에 특정 키워드가 포함되면 해당 상품으로 인식
   ========================= */
const menuMap: Record<string, { sku: string; price: number; display?: string }> = {
  "아메리카노": { sku: "AMER", price: 3000 },
  "라떼": { sku: "LATTE", price: 3800 },
  "쿠키": { sku: "COOKIE", price: 1500 },
  "머핀": { sku: "MUFFIN", price: 2500 },
  "샌드위치": { sku: "SAND", price: 5200 },
};

/* =========================
   3) 영수증 빌더
   - 대화 로그(턴들) -> 품목 집계 -> 합계 계산 -> receipt 생성
   ========================= */
function buildReceipt(params: {
  scenarioId: number;
  sessionId: string;
  storeName: string;
  turns: SessionTurn[];
  startedAt: string;
  finishedAt: string;
  payment: Receipt["payment"];
}): Receipt {
  // 1) 키워드 매칭으로 주문 아이템 집계
  const bucket = new Map<string, { name: string; sku: string; qty: number; unitPrice: number }>();
  for (const t of params.turns) {
    const key = Object.keys(menuMap).find((k) => t.selectedOptionText.includes(k));
    if (!key) continue;
    const { sku, price, display } = menuMap[key];
    const cur = bucket.get(sku) ?? { name: display ?? key, sku, qty: 0, unitPrice: price };
    cur.qty += 1;
    bucket.set(sku, cur);
  }

  // 2) 라인아이템과 금액 합계
  const items: LineItem[] = Array.from(bucket.values()).map((it) => ({
    ...it,
    total: it.qty * it.unitPrice,
  }));
  const subtotal = items.reduce((s, it) => s + it.total, 0);
  const discount = 0;
  const tax = Math.round(subtotal * 0.1); // 예시: 부가세 10%
  const total = subtotal + tax - discount;

  // 3) 간단한 영수증 ID 생성
  const receiptId = `R-${params.sessionId.slice(0, 6)}-${Date.now().toString(36).toUpperCase()}`;

  return {
    receiptId,
    store: { name: params.storeName },
    scenarioId: params.scenarioId,
    sessionId: params.sessionId,
    startedAt: params.startedAt,
    finishedAt: params.finishedAt,
    items,
    subtotal,
    tax,
    discount,
    total,
    payment: params.payment,
  };
}

/* =========================
   4) 헬퍼: 안전한 통화 포맷
   ========================= */
const W = (n: number) => `${n.toLocaleString("ko-KR")}원`;

/* =========================
   5) 스타일 (Tailwind 없어도 인쇄 예쁘게)
   - print 스타일 포함
   ========================= */
const styles: Record<string, React.CSSProperties> = {
  root: {
    maxWidth: 420,
    margin: "24px auto",
    background: "#fff",
    padding: 16,
    borderRadius: 12,
    boxShadow: "0 6px 24px rgba(0,0,0,0.08)",
    fontFamily:
      "-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Noto Sans KR', 'Apple SD Gothic Neo', 'Malgun Gothic', sans-serif",
    color: "#111827",
  },
  header: { textAlign: "center" as const, fontWeight: 800, fontSize: 18 },
  sub: { marginTop: 8, fontSize: 12, color: "#6B7280", lineHeight: 1.4 },
  row: { display: "flex", justifyContent: "space-between", alignItems: "center", marginTop: 6 },
  divider: { marginTop: 12, marginBottom: 8, borderTop: "1px dashed #D1D5DB" },
  totalRow: { display: "flex", justifyContent: "space-between", alignItems: "center", marginTop: 8, fontWeight: 700 },
  faint: { color: "#6B7280" },
  btnBar: { display: "flex", gap: 8, marginTop: 16 },
  btn: {
    padding: "8px 12px",
    borderRadius: 8,
    border: "1px solid #E5E7EB",
    background: "#F3F4F6",
    cursor: "pointer",
    fontSize: 13,
  },
  badge: {
    display: "inline-block",
    padding: "2px 8px",
    borderRadius: 999,
    background: "#EEF2FF",
    color: "#3730A3",
    fontSize: 11,
    fontWeight: 700,
    marginLeft: 8,
  },
};

/* =========================
   6) 모의 데이터 (라우터/전역 상태가 없을 때 자동 사용)
   ========================= */
function getMock(): {
  turns: SessionTurn[];
  startedAt: string;
  finishedAt: string;
  scenarioId: number;
  sessionId: string;
  payment: Receipt["payment"];
  storeName: string;
} {
  const now = new Date();
  const started = new Date(now.getTime() - 1000 * 60 * 3); // 3분 전
  const mockTurns: SessionTurn[] = [
    {
      sequenceNumber: 1,
      question: "무엇을 도와드릴까요?",
      selectedOptionId: 11,
      selectedOptionText: "아메리카노 주세요.",
      correct: true,
      timestamp: started.toISOString(),
    },
    {
      sequenceNumber: 2,
      question: "사이즈는 어떻게 드릴까요?",
      selectedOptionId: 21,
      selectedOptionText: "라지로 주세요.",
      correct: true,
      timestamp: new Date(started.getTime() + 20000).toISOString(),
    },
    {
      sequenceNumber: 3,
      question: "추가로 드실 건 없을까요?",
      selectedOptionId: 31,
      selectedOptionText: "쿠키도 하나 주세요.",
      correct: true,
      timestamp: new Date(started.getTime() + 40000).toISOString(),
    },
  ];
  return {
    turns: mockTurns,
    startedAt: started.toISOString(),
    finishedAt: now.toISOString(),
    scenarioId: 1,
    sessionId: crypto.randomUUID(),
    payment: { method: "CARD", masked: "****-****-****-1234" },
    storeName: "마주카페 시뮬레이터",
  };
}

/* =========================
   7) 페이지 컴포넌트
   - globalThis.__RECEIPT_STATE__ 가 있으면 실제 데이터 사용
   - 없으면 모의 데이터로 렌더
   ========================= */
export default function ReceiptPage() {
  // 외부에서 주입 가능한 상태 (예: 시나리오 종료 시)
  //   (globalThis as any).__RECEIPT_STATE__ = {
  //     turns, startedAt, finishedAt, scenarioId, sessionId, payment, storeName
  //   };
  const injected = (globalThis as any).__RECEIPT_STATE__;
  const input = injected ?? getMock();

  const receipt = useMemo(
    () =>
      buildReceipt({
        scenarioId: input.scenarioId,
        sessionId: input.sessionId,
        storeName: input.storeName,
        turns: input.turns,
        startedAt: input.startedAt,
        finishedAt: input.finishedAt,
        payment: input.payment,
      }),
    [input]
  );

  return (
    <div style={styles.root}>
      <h2 style={styles.header}>
        {receipt.store.name}
        <span style={styles.badge}>영수증</span>
      </h2>
      <div style={styles.sub}>
        영수증번호: <b>{receipt.receiptId}</b>
        <br />
        시나리오: #{receipt.scenarioId} · 세션: {receipt.sessionId.slice(0, 8)}
        <br />
        시작: {fmtKST(receipt.startedAt)} · 종료: {fmtKST(receipt.finishedAt)}
      </div>

      <div style={styles.divider} />

      {/* 품목 리스트 */}
      {receipt.items.length === 0 ? (
        <div style={{ ...styles.row, color: "#6B7280" }}>주문 품목이 없습니다.</div>
      ) : (
        receipt.items.map((i) => (
          <div key={i.sku} style={styles.row}>
            <span>
              {i.name} ×{i.qty}
            </span>
            <span>{W(i.total)}</span>
          </div>
        ))
      )}

      <div style={styles.divider} />

      {/* 합계 */}
      <div style={styles.row}>
        <span style={styles.faint}>소계</span>
        <span>{W(receipt.subtotal)}</span>
      </div>
      <div style={styles.row}>
        <span style={styles.faint}>부가세</span>
        <span>{W(receipt.tax)}</span>
      </div>
      {receipt.discount > 0 && (
        <div style={styles.row}>
          <span style={styles.faint}>할인</span>
          <span>-{W(receipt.discount)}</span>
        </div>
      )}
      <div style={styles.totalRow}>
        <span>합계</span>
        <span>{W(receipt.total)}</span>
      </div>

      <div style={styles.divider} />

      <div style={{ ...styles.row, fontSize: 13 }}>
        결제수단: <b>{receipt.payment.method}</b>
        <span style={styles.faint}>{receipt.payment.masked ? ` ${receipt.payment.masked}` : ""}</span>
      </div>

      {/* 버튼 영역 (인쇄/새로고침) — print 시 감춤 */}
      <div className="print-hidden" style={styles.btnBar}>
        <button style={styles.btn} onClick={() => window.print()}>
          인쇄 / PDF 저장
        </button>
        <button
          style={styles.btn}
          onClick={() => {
            if (confirm("모의 데이터로 새 영수증을 재생성할까요?")) location.reload();
          }}
        >
          새로고침(모의)
        </button>
      </div>

      {/* 인쇄 시 버튼 숨김 */}
      <style>{`@media print {.print-hidden{ display:none !important; } body{ background:#fff; }}`}</style>
    </div>
  );
}

/* =========================
   8) 유틸: KST 포맷
   ========================= */
function fmtKST(iso: string) {
  const date = new Date(iso);
  return new Intl.DateTimeFormat("ko-KR", {
    timeZone: "Asia/Seoul",
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  }).format(date);
}

/* =========================
   9) (선택) 외부에서 실제 데이터 주입 예시
   - 시나리오 종료 시 다음처럼 세팅 후 /receipt 라우트로 이동하면 실데이터 렌더
   =========================
(globalThis as any).__RECEIPT_STATE__ = {
  turns: sessionTurns,               // SessionTurn[]
  startedAt,                         // ISO string
  finishedAt,                        // ISO string
  scenarioId,                        // number
  sessionId: crypto.randomUUID(),    // string
  payment: { method: 'CARD', masked: '****-****-****-1234' },
  storeName: '마주카페 시뮬레이터',
};
*/ 
