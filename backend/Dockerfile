# ==========================================
# Build stage - 최적화된 Gradle 빌드
# ==========================================
FROM gradle:8.5-jdk21 AS build
WORKDIR /app

# 1단계: Gradle Wrapper와 설정 파일만 복사 (거의 변경 안 됨)
COPY gradle gradle
COPY gradlew .
COPY settings.gradle .

# 2단계: build.gradle 복사 및 의존성 다운로드 (의존성 변경 시에만 재실행)
COPY build.gradle .
RUN gradle dependencies --no-daemon --stacktrace

# 3단계: 소스 코드 복사 및 빌드 (코드 변경 시에만 재실행)
COPY src src
RUN gradle build -x test --no-daemon --parallel

# ==========================================
# Runtime stage - 경량 JRE 이미지
# ==========================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 비 root 사용자로 실행 (보안)
RUN addgroup -S spring && adduser -S spring -G spring

# 로그 디렉토리 생성 및 권한 부여
RUN mkdir -p /app/logs && chown -R spring:spring /app

# 빌드된 JAR 파일만 복사
COPY --from=build /app/build/libs/*.jar app.jar
RUN chown spring:spring app.jar

# spring 사용자로 전환
USER spring:spring

# 8080 포트 노출
EXPOSE 8080

# JVM 메모리 설정 및 애플리케이션 실행
ENTRYPOINT ["java", \
    "-Xms256m", \
    "-Xmx512m", \
    "-XX:+UseContainerSupport", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", \
    "app.jar"]