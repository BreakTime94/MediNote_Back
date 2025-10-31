# ======================================================
# Stage 1 — Build 단계 (Gradle 빌드 전용)
# ======================================================
FROM gradle:8.8-jdk21 AS builder

# 작업 디렉토리 설정 (빌드 기준점)
WORKDIR /app

# Gradle 설정 파일 및 Wrapper 복사
COPY build.gradle settings.gradle gradlew /app/
COPY gradle /app/gradle

# Gradle 캐시 디렉터리 설정
ENV GRADLE_USER_HOME=/cache

# Gradle 의존성 미리 다운로드 (캐시화)
RUN ./gradlew dependencies --no-daemon || true

# 소스코드 복사
COPY src /app/src

# Gradle 빌드 (테스트 생략)
RUN ./gradlew clean build -x test -- no-daemon


# ======================================================
# Stage 2 — Runtime 단계 (경량 실행용)
# ======================================================
FROM openjdk:21-jdk-slim

# 실행용 작업 디렉토리 설정
WORKDIR /app

# Stage 1에서 빌드된 jar 파일만 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 컨테이너 내부 포트 노출
EXPOSE 8083

# 한글 로그 깨짐 방지용 환경 변수
ENV LANG=ko_KR.UTF-8
ENV LC_ALL=ko_KR.UTF-8

# 컨테이너 시작 시 실행할 명령
ENTRYPOINT ["java", "-jar", "app.jar"]
