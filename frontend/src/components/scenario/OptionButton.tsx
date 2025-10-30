/** @format */

/** @format */

interface OptionProps {
  onClick?: () => void;
  children: React.ReactNode;
  color?: "pink" | "yellow" | "green" | "blue" | "white";
}

export default function OptionButton({
  onClick,
  children,
  color = "white",
}: OptionProps) {
  const colorMap = {
    pink: "bg-pink-200 hover:bg-pink-300 text-gray-800",
    yellow: "bg-yellow-100 hover:bg-yellow-200 text-gray-800",
    green: "bg-green-200 hover:bg-green-300 text-gray-800",
    blue: "bg-blue-200 hover:bg-blue-300 text-gray-800",
    white: "bg-white hover:bg-gray-100 text-gray-800",
  };

  return (
    <button
      onClick={onClick}
      className={`font-shark w-full max-w-xs py-3 px-4 rounded-2xl text-center text-lg font-medium shadow-md transition ${colorMap[color]} `}
    >
      {children}
    </button>
  );
}
