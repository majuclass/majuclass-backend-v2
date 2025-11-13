/**
 * @format
 * @type {import('tailwindcss').Config}
 */

export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      fontFamily: {
        shark: ['PinkfongBabyShark', 'sans-serif'],
        nsr: ['NanumSquareRound', 'sans-serif'],
        nsrLight: ['NanumSquareRoundLight', 'sans-serif'],
        nsrBold: ['NanumSquareRoundBold', 'sans-serif'],
        nsrExtraBold: ['NanumSquareRoundExtraBold', 'sans-serif'],
      },
      fontSize: {
        xs: '1rem', // 원래 0.75rem (12px) → 16px
        sm: '1.125rem', // 원래 0.875rem (14px) → 18px
        base: '1.25rem', // 원래 1rem (16px) → 20px
        lg: '1.5rem', // 원래 1.125rem (18px) → 24px
        xl: '1.75rem', // 원래 1.25rem (20px) → 28px
        '2xl': '2rem', // 원래 1.5rem (24px) → 32px
        '3xl': '2.5rem', // 원래 1.875rem (30px) → 40px
      },
    },
  },
  plugins: [],
};
