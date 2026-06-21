FROM node:25-alpine AS frontend-build

WORKDIR /app/frontend

COPY frontend/package.json frontend/package-lock.json* ./
RUN npm ci

COPY frontend/ ./
RUN npm run build -- --configuration=production

FROM eclipse-temurin:21-jdk AS backend-build

WORKDIR /app

COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

COPY src/ src/
COPY --from=frontend-build /app/frontend/dist/url-shortener-frontend/browser/ src/main/resources/static/

RUN ./mvnw package -DskipTests -B

FROM eclipse-temurin:21-jre AS runtime

WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

RUN groupadd --system appgroup && useradd --system --gid appgroup appuser

COPY --from=backend-build /app/target/*.jar app.jar
RUN chown appuser:appgroup app.jar
USER appuser

ENV PORT=8080
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=70s --retries=3 \
  CMD curl -fsS http://localhost:${PORT}/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
