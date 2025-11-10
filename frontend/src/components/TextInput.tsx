/** @format */

// import { useState } from "react";

interface TextInputProps {
  children: React.ReactNode;
  name?: string;
  placeholder?: string;
  type?: string;
  onChange?: (value: string) => void;
  value: string;
}

/** CreateScenario에 주로 사용되는 TextInput (store 사용용)
 * @prop {React.ReactNode} children - 레이블
 * @prop {string} [name] - input 구분하는 이름
 * @prop {string} [placeholder] - input에 사용되는 이름
 * @prop {string} [type='text'] - input의 타입 (기본 text)
 * @prop {function(string): void} [onChange] -
 * @prop {string} value
 */
export default function TextInput({
  children,
  name,
  placeholder,
  type = "text",
  onChange,
  value,
}: TextInputProps) {
  // const [inputValue, setInputValue] = useState("");
  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    // 내부 update 제거: 전역변수 사용
    // setInputValue(e.target.value);
    // lift up
    if (onChange) onChange(e.target.value);
  };

  const handleClear = () => {
    // 내부 업데이트 제거
    // setInputValue("");
    // 외부로 빈 문자열 전달 (store 리셋)
    if (onChange) onChange("");
  };

  return (
    <div className="flex flex-row gap-3 items-center">
      <p>{children}</p>
      <div className="relative flex items-center">
        <input
          type={type}
          value={value}
          onChange={handleInputChange}
          name={name}
          placeholder={placeholder}
          className="pr-10 py-2 border rounded shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
        />

        {/* input 있을때만 x버튼 렌더링 */}
        {value && (
          <button
            onClick={handleClear}
            className="absolute right-2 p-1 text-gray-400 hover:text-gray-600 focus:outline-none"
          >
            X
          </button>
        )}
      </div>
    </div>
  );
}
