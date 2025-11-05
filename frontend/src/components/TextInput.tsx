/** @format */

import { useState } from "react";

interface TextInputProps {
  children: React.ReactNode;
  name?: string;
  placeholder?: string;
  type?: string;
}

/** CreateScenario 생성에 사용되는 TextInput
 * @prop {React.ReactNode} children - 레이블
 * @prop {string} [name] - input 구분하는 이름
 * @prop {string} [placeholder] - input에 사용되는 이름
 * @prop {string} [type='text'] - input의 타입 (기본 text)
 */
export default function TextInput({
  children,
  name,
  placeholder,
  type = "text",
}: TextInputProps) {
  const [inputValue, setInputValue] = useState("");

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setInputValue(e.target.value);
  };

  const handleClear = () => {
    setInputValue("");
  };

  return (
    <div className="flex flex-row gap-3 items-center">
      <p>{children}</p>
      <div className="relative flex items-center">
        <input
          type={type}
          value={inputValue}
          onChange={handleInputChange}
          name={name}
          placeholder={placeholder}
          className="pr-10 py-2 border rounded shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
        />

        {/* input 있을때만 x버튼 렌더링 */}
        {inputValue && (
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
