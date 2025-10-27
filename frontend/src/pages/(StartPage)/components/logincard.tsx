import { useEffect, useState } from "react";
import { Link } from "react-router-dom";

type Props = {
  onLogin?: (payload: { id: string; pw: string; remember: boolean }) => Promise<void> | void;
};

export default function LoginCard({ onLogin }: Props) {
  const [id, setId] = useState("");
  const [pw, setPw] = useState("");
  const [showPw, setShowPw] = useState(false);
  const [remember, setRemember] = useState(false);
  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState<string | null>(null);

  // 아이디 저장 옵션 유지
  useEffect(() => {
    const savedRemember = localStorage.getItem("remember_id") === "1";
    const savedId = localStorage.getItem("saved_id") || "";
    setRemember(savedRemember);
    if (savedRemember && savedId) setId(savedId);
  }, []);

  useEffect(() => {
    if (remember) {
      localStorage.setItem("remember_id", "1");
      localStorage.setItem("saved_id", id);
    } else {
      localStorage.setItem("remember_id", "0");
      localStorage.removeItem("saved_id");
    }
  }, [remember, id]);

  const canSubmit = id.trim().length > 0 && pw.trim().length > 0 && !loading;

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!canSubmit) return;

    try {
      setErr(null);
      setLoading(true);
      if (onLogin) {
        await onLogin({ id: id.trim(), pw, remember });
      } else {
        // TODO: 실제 API 연동
        console.log({ id: id.trim(), pw, remember });
        await new Promise((r) => setTimeout(r, 600)); // 데모용 딜레이
      }
    } catch (error: unknown) {
      let msg = "로그인에 실패했어요. 다시 시도해주세요.";
      if (error instanceof Error) msg = error.message;
      else if (typeof error === "string") msg = error;
      setErr(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-card" role="form" aria-labelledby="login-title">
      <h1 className="login-title" id="login-title">Login</h1>

      <form onSubmit={onSubmit} className="login-form" noValidate>
        {/* 아이디 */}
        <div className="field">
          <label htmlFor="login-id" className="sr-only">아이디</label>
          <input
            id="login-id"
            name="id"
            type="text"
            placeholder="아이디"
            value={id}
            onChange={(e) => setId(e.target.value)}
            autoComplete="username"
            inputMode="text"
            spellCheck={false}
            aria-invalid={!!err}
          />
        </div>

        {/* 비밀번호 */}
        <div className="field pw-field">
          <label htmlFor="login-pw" className="sr-only">비밀번호</label>
          <input
            id="login-pw"
            name="password"
            type={showPw ? "text" : "password"}
            placeholder="비밀번호"
            value={pw}
            onChange={(e) => setPw(e.target.value)}
            autoComplete="current-password"
            aria-invalid={!!err}
          />
          <button
            type="button"
            className="pw-toggle"
            onClick={() => setShowPw((v) => !v)}
            aria-label={showPw ? "비밀번호 감추기" : "비밀번호 표시"}
            aria-pressed={showPw}
            aria-controls="login-pw"
          >
            {showPw ? "🙈" : "👁️"}
          </button>
        </div>

        {/* 아이디 저장 */}
        <label className="remember">
          <input
            type="checkbox"
            checked={remember}
            onChange={(e) => setRemember(e.target.checked)}
          />
          아이디 저장
        </label>

        {/* 에러 메시지 */}
        {err && (
          <p role="alert" style={{ color: "#b42318", fontSize: 14, margin: "4px 0 0" }}>
            {err}
          </p>
        )}

        {/* 로그인 버튼 */}
        <button className="submit" disabled={!canSubmit}>
          {loading ? "로그인 중..." : "로그인"}
        </button>

        <p className="signup">
          아직 회원이 아니신가요? <Link to="/signup">회원가입하기</Link>
        </p>
      </form>
    </div>
  );
}
