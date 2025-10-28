/** @format */

interface OptionProps {
  onClick?: () => void;
  children: React.ReactNode;
}

export default function OptionButton({ onClick, children }: OptionProps) {
  return (
    <button
      onClick={onClick}
      className="w-full max-w-xs py-3 rounded-2xl text-center text-lg font-medium shadow bg-white hover:bg-gray-100 transition"
    >
      {children}
    </button>
  );
}
