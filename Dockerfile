# ── Stage 1: build fat JAR ──────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copy Gradle wrapper first — changes rarely, so this layer is cached
COPY gradlew gradlew
COPY gradle gradle
COPY gradle.properties gradle.properties
COPY build.gradle.kts settings.gradle.kts ./

# Download dependencies (re-runs only when build files change)
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon

# Copy source and build
COPY src src
RUN ./gradlew buildFatJar --no-daemon

# ── Stage 2: minimal runtime image ──────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /app/build/libs/todapp-0.0.1-all.jar app.jar

EXPOSE 8080

# -Xmx200m keeps the JVM within Fly.io's free 256 MB limit
ENTRYPOINT ["java", "-Xmx200m", "-jar", "app.jar"]
