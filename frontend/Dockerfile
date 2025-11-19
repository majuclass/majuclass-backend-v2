# 1) 빌드
FROM node:20 AS build
WORKDIR /app

# 의존성 캐시
COPY package*.json ./
RUN npm ci

# Jenkins가 stage에서 frontend/.env를 내려줌 → 아래 COPY . . 에 포함됨
COPY . .
RUN npm run build

# 2) 정적 서빙
FROM nginx:stable-alpine

# 기본 conf 제거 후 SPA용 conf 등록
RUN rm -f /etc/nginx/conf.d/default.conf
COPY nginx/react.conf /etc/nginx/conf.d/default.conf

# 빌드 산출물 복사
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80
HEALTHCHECK CMD wget -qO- http://127.0.0.1 >/dev/null 2>&1 || exit 1
CMD ["nginx", "-g", "daemon off;"]
