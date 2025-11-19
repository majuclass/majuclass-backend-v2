import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    // allowedHosts: ["7df3064413d0.ngrok-free.app"], // 테스트용 서버 주소이며, 매번 수정해주어야 함
  },
})
