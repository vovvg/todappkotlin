# ── Stage 1: build fat JAR ──────────────────────────────────────────────────
FROM gradle:8-jdk21-alpine AS builder
WORKDIR /app

# Copy build files first — dependencies layer is cached until they change
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
RUN gradle dependencies --no-daemon

# Copy source and build
COPY src src
RUN gradle buildFatJar --no-daemon

# ── Stage 2: minimal runtime image ──────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /app/build/libs/todapp-0.0.1-all.jar app.jar

EXPOSE 8080

# -Xmx200m keeps the JVM within Render's free 512 MB limit
ENTRYPOINT ["java", "-Xmx200m", "-jar", "app.jar"]
