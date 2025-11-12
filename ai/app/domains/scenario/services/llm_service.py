import os
import json
import re
from typing import Any, Dict, List

import httpx

GMS_BASE = os.getenv("GMS_BASE")
GMS_KEY  = os.getenv("GMS_KEY")


class LLMService:
    """
    GMS(gpt-4.1)로 '짧은 텍스트' 중심의 시나리오를 생성한다.
    - 결과 스키마:
      {
        "title": str,
        "summary": str,
        "sequences": [
          {
            "seqNo": int,
            "question": str,
            "options": [
              {"optionNo": int, "optionText": str, "isAnswer": bool}
            ]
          }, ...
        ]
      }
    - 질문/보기 개수 엄수, 각 질문 정답은 정확히 1개
    - optionText는 8~12자 내외의 매우 짧은 라벨형 문구로 정규화
    """

    def __init__(self, timeout: float = 90.0, model: str = "gpt-4.1"):
        if not GMS_KEY:
            raise RuntimeError("GMS_KEY is missing in environment")
        self._timeout = timeout
        self._model = model
        self._headers = {
            "Authorization": f"Bearer {GMS_KEY}",
            "Content-Type": "application/json",
        }

    # ------------------------ Public API ------------------------

    async def generate_scenario_text(
        self,
        *,
        prompt: str,
        question_count: int,
        options_per_question: int,
        retries: int = 1,
    ) -> Dict[str, Any]:
        """
        시나리오 텍스트(JSON) 생성.
        - 검증/정규화까지 마친 dict를 반환.
        - 실패 시 temperature를 낮추고 1회까지 재시도 가능.
        """
        attempt = 0
        last_err: Exception | None = None
        while attempt <= retries:
            try:
                raw = await self._call_gms(prompt, question_count, options_per_question)
                data = self._normalize_and_validate(raw, question_count, options_per_question)
                return data
            except Exception as e:
                last_err = e
                attempt += 1
        raise RuntimeError(f"LLM scenario generation failed after retries: {last_err}")

    # ------------------------ Internals ------------------------

    async def _call_gms(self, prompt: str, q_cnt: int, o_cnt: int) -> Dict[str, Any]:
        system = self._system_schema(q_cnt, o_cnt)
        messages = [
            {"role": "developer", "content": "Answer in Korean"},
            {"role": "system", "content": system},
            {"role": "user", "content": json.dumps({"prompt": prompt}, ensure_ascii=False)},
        ]
        payload = {
            "model": self._model,
            "messages": messages,
            "response_format": {"type": "json_object"},
            "temperature": 0.4,
            "max_tokens": 4096,
        }
        async with httpx.AsyncClient(timeout=self._timeout) as c:
            r = await c.post(f"{GMS_BASE}/chat/completions", headers=self._headers, json=payload)
            r.raise_for_status()
            content = r.json()["choices"][0]["message"]["content"]
            return json.loads(content)

    def _system_schema(self, q_cnt: int, o_cnt: int) -> str:
        return (
            "오직 JSON만 출력.\n"
            '스키마: {"title":string,"summary":string,'
            '"sequences":[{"seqNo":number,"question":string,'
            '"options":[{"optionNo":number,"optionText":string,"isAnswer":boolean}]}]}\n'
            f"- 질문 개수: {q_cnt}개, 보기 개수: {o_cnt}개 (엄수)\n"
            "- 각 질문의 정답(isAnswer=true)은 정확히 1개\n"
            "- optionText는 아주 짧은 라벨형 문구(8~12자, 문장부호/이모지/존칭/구어체 금지)\n"
            "- 명확하고 교육 친화적인 표현, 한국어\n"
        )

    def _normalize_and_validate(self, data: Dict[str, Any], q_cnt: int, o_cnt: int) -> Dict[str, Any]:
        title = str(data.get("title", "")).strip()
        summary = str(data.get("summary", "")).strip()
        seqs: List[Dict[str, Any]] = data.get("sequences", [])
        if not isinstance(seqs, list):
            raise ValueError("sequences must be a list")

        if len(seqs) != q_cnt:
            raise ValueError(f"question_count mismatch: expected {q_cnt}, got {len(seqs)}")

        norm_seqs: List[Dict[str, Any]] = []
        for i, seq in enumerate(seqs, start=1):
            question = str(seq.get("question", "")).strip()
            options = seq.get("options", [])
            if not isinstance(options, list):
                raise ValueError(f"options must be a list at seq {i}")
            if len(options) != o_cnt:
                raise ValueError(f"options_per_question mismatch at seq {i}: expected {o_cnt}, got {len(options)}")

            norm_opts = []
            true_cnt = 0
            seen_texts: set[str] = set()

            for j, opt in enumerate(options, start=1):
                is_true = bool(opt.get("isAnswer"))
                if is_true:
                    true_cnt += 1
                text = self._clamp_short_label(str(opt.get("optionText", "")))
                # 중복 라벨 방지(희박하지만 안전장치)
                if text in seen_texts:
                    text = self._dedup_label(text, j)
                seen_texts.add(text)

                norm_opts.append({
                    "optionNo": j,
                    "optionText": text,
                    "isAnswer": is_true,
                })

            if true_cnt != 1:
                raise ValueError(f"seq {i}: exactly one isAnswer=true required (got {true_cnt})")

            norm_seqs.append({
                "seqNo": i,
                "question": question,
                "options": norm_opts,
            })

        return {"title": title, "summary": summary, "sequences": norm_seqs}

    # ---- helpers: short label normalization ----

    def _clamp_short_label(self, s: str, min_len: int = 2, max_len: int = 12) -> str:
        """
        너무 긴/지저분한 텍스트를 라벨형으로 정리한다.
        - 허용: 한글/영문/숫자/공백
        - 제거: 이모지/특수문자/문장부호
        - 길이: 8~12자 권장, 여기선 [min_len, max_len]로 클램프
        """
        s = re.sub(r"[^\w가-힣\s]", "", s)       # 특수문자 제거
        s = re.sub(r"\s+", " ", s).strip()      # 공백 정리
        if len(s) > max_len:
            s = s[:max_len].rstrip()
        if len(s) < min_len:
            s = (s + " 옵션")[:min_len] if s else "옵션"
        return s

    def _dedup_label(self, text: str, idx: int) -> str:
        # 간단 dedup(뒤에 숫자 부가). 여전히 12자 제한 유지.
        suffix = f"{idx}"
        base = text[: max(1, 12 - len(suffix) - 1)]
        return f"{base}-{suffix}"
