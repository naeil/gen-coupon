# =========================
# 1️⃣ Build stage
# =========================
FROM public.ecr.aws/docker/library/gradle:9.2.1-jdk21-jammy AS builder

WORKDIR /build

# Gradle 캐시 최적화
COPY build.gradle settings.gradle gradlew ./
COPY gradle gradle
RUN ./gradlew dependencies --no-daemon || true

# 소스 복사
COPY src src

# bootJar 생성
RUN ./gradlew bootJar --no-daemon


# =========================
# 2️⃣ Runtime stage
# =========================
FROM public.ecr.aws/docker/library/eclipse-temurin:21-jre-jammy

WORKDIR /naeil

# builder stage에서 생성된 jar만 복사
COPY --from=builder /build/build/libs/gen-coupon.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java","-jar","app.jar"]
