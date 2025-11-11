import React, { useEffect, useMemo, useRef, useState } from "react";

/**
 * PictogramPopover
 * - Notion 아이콘 선택기 느낌의 팝오버
 * - 프로젝트 구조: frontend/src/assets/pictogram/(카테고리)/(이미지)
 * - Vite 의 import.meta.glob 로 정적 자산 자동 수집
 *
 * 사용법
 * <PictogramPopover
 *   open={open}
 *   onClose={() => setOpen(false)}
 *   onSelect={(item) => { console.log(item); setOpen(false); }}
 *   anchorRef={buttonRef}
 * />
 */

export type PictogramItem = {
  id: string; // unique path 기반 id
  name: string; // 파일명(확장자 제거)
  category: string; // 상위 폴더명
  src: string; // 정적 자산 url
};

type PictogramPopoverProps = {
  open: boolean;
  onClose: () => void;
  onSelect: (item: PictogramItem) => void;
  anchorRef: React.RefObject<HTMLElement | null>; // 트리거 버튼 ref (위치 기준)
  /** 팝오버 최대 높이(px) */
  maxHeight?: number;
};

// ==== 자산 스캔: /src/assets/pictogram/**/*.{png,svg,jpg,jpeg} ====
function usePictograms() {
  // glob 결과는 { path: module } 형태로 반환, module default 가 URL
  const modules = import.meta.glob(
    "/src/assets/pictogram/**/*.{png,svg,jpg,jpeg}",
    { eager: true, import: "default" }
  ) as Record<string, string>;

  const list: PictogramItem[] = useMemo(() => {
    return Object.entries(modules).map(([path, url]) => {
      // path 예: "/src/assets/pictogram/cafe/americano.png"
      const parts = path.split("/");
      const file = parts[parts.length - 1];
      const category = parts[parts.length - 2] || "etc";
      const name = file.replace(/\.[^.]+$/, "");
      return {
        id: path,
        name,
        category,
        src: url,
      } as PictogramItem;
    });
  }, []);

  // 카테고리 목록
  const categories = useMemo(() => {
    const set = new Set<string>();
    list.forEach((i) => set.add(i.category));
    return Array.from(set).sort((a, b) => a.localeCompare(b));
  }, [list]);

  return { list, categories };
}

// ==== 최근 항목 로컬스토리지 ====
const RECENT_KEY = "recentPictograms";
function useRecent(max = 18) {
  const [recent, setRecent] = useState<PictogramItem[]>([]);

  useEffect(() => {
    const raw = localStorage.getItem(RECENT_KEY);
    if (raw) {
      try {
        const parsed = JSON.parse(raw) as PictogramItem[];
        setRecent(parsed);
      } catch {
        /* noop */
      }
    }
  }, []);

  const push = (item: PictogramItem) => {
    setRecent((prev) => {
      const next = [item, ...prev.filter((p) => p.id !== item.id)].slice(
        0,
        max
      );
      localStorage.setItem(RECENT_KEY, JSON.stringify(next));
      return next;
    });
  };

  return { recent, push };
}

// ==== 팝오버 본체 ====
export default function PictogramPopover({
  open,
  onClose,
  onSelect,
  anchorRef,
  maxHeight = 520,
}: PictogramPopoverProps) {
  const { list, categories } = usePictograms();
  const { recent, push } = useRecent();

  const [tab, setTab] = useState<"icons" | "upload">("icons");
  const [query, setQuery] = useState("");
  const [activeCategory, setActiveCategory] = useState<string | "ALL">(
    "ALL"
  );
  const popRef = useRef<HTMLDivElement>(null);

  // 바깥 클릭 닫기
  useEffect(() => {
    if (!open) return;
    const handler = (e: MouseEvent) => {
      if (
        popRef.current &&
        !popRef.current.contains(e.target as Node) &&
        !anchorRef.current?.contains(e.target as Node)
      ) {
        onClose();
      }
    };
    document.addEventListener("mousedown", handler);
    return () => document.removeEventListener("mousedown", handler);
  }, [open, onClose, anchorRef]);

  // 위치 계산 (anchor 의 좌하단 기준)
  const [position, setPosition] = useState<{ top: number; left: number }>(
    { top: 0, left: 0 }
  );
  useEffect(() => {
    if (open && anchorRef.current) {
      const rect = anchorRef.current.getBoundingClientRect();
      setPosition({ top: rect.bottom + window.scrollY + 8, left: rect.left + window.scrollX });
    }
  }, [open, anchorRef]);

  // 필터링
  const filtered = useMemo(() => {
    const q = query.trim().toLowerCase();
    return list.filter((i) => {
      const okCat = activeCategory === "ALL" || i.category === activeCategory;
      const okQuery = !q ||
        i.name.toLowerCase().includes(q) ||
        i.category.toLowerCase().includes(q);
      return okCat && okQuery;
    });
  }, [list, query, activeCategory]);

  const handlePick = (item: PictogramItem) => {
    push(item);
    onSelect(item);
  };

  if (!open) return null;

  return (
    <div
      ref={popRef}
      style={{
        position: "absolute",
        top: position.top,
        left: position.left,
        width: 560,
        background: "#fff",
        borderRadius: 12,
        boxShadow:
          "0 10px 24px rgba(0,0,0,0.12), 0 2px 6px rgba(0,0,0,0.06)",
        border: "1px solid #E5E7EB",
        overflow: "hidden",
        zIndex: 1000,
      }}
      role="dialog"
      aria-modal
    >
      {/* 헤더: 탭 + 닫기 */}
      <div style={{ display: "flex", alignItems: "center", padding: "10px 12px", gap: 8, borderBottom: "1px solid #F1F5F9" }}>
        <button
          onClick={() => setTab("icons")}
          style={tabBtnStyle(tab === "icons")}
        >
          아이콘
        </button>
        <button
          onClick={() => setTab("upload")}
          style={tabBtnStyle(tab === "upload")}
        >
          업로드
        </button>
        <div style={{ marginLeft: "auto" }}>
          <button onClick={onClose} aria-label="닫기" style={iconBtnStyle}>
            ×
          </button>
        </div>
      </div>

      {tab === "icons" ? (
        <div style={{ display: "grid", gridTemplateColumns: "200px 1fr" }}>
          {/* 좌측: 검색 + 카테고리 */}
          <div style={{ borderRight: "1px solid #F1F5F9", padding: 12 }}>
            <div style={{ position: "relative", marginBottom: 12 }}>
              <input
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                placeholder="필터"
                style={searchInputStyle}
              />
            </div>

            <div style={{ fontSize: 12, color: "#6B7280", margin: "8px 0" }}>카테고리</div>
            <div style={{ display: "flex", flexDirection: "column", gap: 6, maxHeight: maxHeight - 140, overflow: "auto" }}>
              <CategoryChip
                label="전체"
                active={activeCategory === "ALL"}
                onClick={() => setActiveCategory("ALL")}
              />
              {categories.map((c) => (
                <CategoryChip key={c} label={c} active={activeCategory === c} onClick={() => setActiveCategory(c)} />
              ))}
            </div>
          </div>

          {/* 우측: 최근 + 그리드 */}
          <div style={{ padding: 12 }}>
            {recent.length > 0 && (
              <section style={{ marginBottom: 10 }}>
                <div style={sectionTitleStyle}>최근 항목</div>
                <IconGrid
                  items={recent}
                  onPick={handlePick}
                  maxHeight={140}
                />
              </section>
            )}

            <section>
              <div style={sectionTitleStyle}>아이콘</div>
              <IconGrid items={filtered} onPick={handlePick} maxHeight={maxHeight} />
            </section>
          </div>
        </div>
      ) : (
        <div style={{ padding: 16 }}>
          <UploadPanel
            onUploaded={(item) => handlePick(item)}
          />
        </div>
      )}
    </div>
  );
}

// ==== 보조 컴포넌트들 ====
function IconGrid({ items, onPick, maxHeight }: { items: PictogramItem[]; onPick: (i: PictogramItem) => void; maxHeight: number; }) {
  return (
    <div
      style={{
        display: "grid",
        gridTemplateColumns: "repeat(9, 1fr)",
        gap: 8,
        maxHeight,
        overflow: "auto",
        paddingRight: 4,
      }}
    >
      {items.map((i) => (
        <button key={i.id} onClick={() => onPick(i)} title={`${i.category} / ${i.name}`} style={iconCellStyle}>
          <img src={i.src} alt={i.name} style={{ width: 28, height: 28, objectFit: "contain" }} />
        </button>
      ))}
    </div>
  );
}

function CategoryChip({ label, active, onClick }: { label: string; active?: boolean; onClick: () => void; }) {
  return (
    <button onClick={onClick} style={{
      textTransform: "none",
      textAlign: "left",
      fontSize: 14,
      padding: "8px 10px",
      borderRadius: 8,
      border: active ? "1px solid #4338CA" : "1px solid transparent",
      background: active ? "#EEF2FF" : "transparent",
      color: active ? "#1F2937" : "#374151",
      cursor: "pointer",
    }}>{label}</button>
  );
}

function UploadPanel({ onUploaded }: { onUploaded: (item: PictogramItem) => void }) {
  const [file, setFile] = useState<File | null>(null);
  const [preview, setPreview] = useState<string | null>(null);

  useEffect(() => {
    if (!file) return setPreview(null);
    const url = URL.createObjectURL(file);
    setPreview(url);
    return () => URL.revokeObjectURL(url);
  }, [file]);

  const handleConfirm = () => {
    if (!file || !preview) return;
    // 업로드 파일도 동일한 스키마로 전달 (runtime blob url)
    const name = file.name.replace(/\.[^.]+$/, "");
    onUploaded({ id: `upload:${file.name}:${Date.now()}` , name, category: "upload", src: preview });
  };

  return (
    <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
      <label style={{ ...btnSolidStyle }}>
        파일 선택
        <input type="file" accept="image/*" style={{ display: "none" }} onChange={(e) => setFile(e.target.files?.[0] ?? null)} />
      </label>
      {preview && (
        <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
          <img src={preview} alt="preview" style={{ width: 36, height: 36, objectFit: "contain", border: "1px solid #E5E7EB", borderRadius: 8 }} />
          <button style={btnPrimaryStyle} onClick={handleConfirm}>이 아이콘 사용</button>
        </div>
      )}
    </div>
  );
}

// ==== 스타일 유틸 ====
const tabBtnStyle = (active: boolean): React.CSSProperties => ({
  padding: "8px 10px",
  borderRadius: 8,
  border: "1px solid",
  borderColor: active ? "#4338CA" : "transparent",
  background: active ? "#EEF2FF" : "transparent",
  color: active ? "#1F2937" : "#374151",
  cursor: "pointer",
  fontWeight: 600,
  fontSize: 14,
});

const iconBtnStyle: React.CSSProperties = {
  width: 28,
  height: 28,
  borderRadius: 8,
  border: "1px solid #E5E7EB",
  background: "#fff",
  cursor: "pointer",
  lineHeight: 1,
  fontSize: 18,
};

const searchInputStyle: React.CSSProperties = {
  width: "100%",
  height: 36,
  borderRadius: 10,
  border: "1px solid #E5E7EB",
  padding: "0 10px",
  outline: "none",
  fontSize: 14,
};

const iconCellStyle: React.CSSProperties = {
  display: "inline-flex",
  alignItems: "center",
  justifyContent: "center",
  width: 44,
  height: 44,
  borderRadius: 10,
  border: "1px solid transparent",
  background: "#fff",
  cursor: "pointer",
} as const;

const sectionTitleStyle: React.CSSProperties = {
  fontSize: 12,
  color: "#6B7280",
  margin: "6px 0 10px",
};

const btnSolidStyle: React.CSSProperties = {
  display: "inline-flex",
  alignItems: "center",
  gap: 8,
  padding: "8px 12px",
  borderRadius: 10,
  border: "1px solid #CBD5E1",
  background: "#fff",
  cursor: "pointer",
  fontSize: 14,
};

const btnPrimaryStyle: React.CSSProperties = {
  padding: "8px 12px",
  borderRadius: 10,
  border: "1px solid #4338CA",
  background: "#4338CA",
  color: "#fff",
  cursor: "pointer",
  fontSize: 14,
  fontWeight: 600,
};
