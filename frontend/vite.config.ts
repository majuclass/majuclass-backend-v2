import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    allowedHosts: ["eeb425caf5eb.ngrok-free.app"], // 매번 수정해주어야 함
  },
})
