# syntax=docker/dockerfile:1

# ---------- Estágio 1: build ----------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# Dependências primeiro: esta camada só é refeita quando o pom.xml muda
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw -B -q dependency:go-offline

# Os testes rodam no CI (precisam de Docker para o Testcontainers); aqui só empacota
COPY src/ src/
RUN ./mvnw -B -q package -DskipTests

# ---------- Estágio 2: runtime ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Usuário sem privilégios
RUN groupadd --system app && useradd --system --gid app --no-create-home app

COPY --from=build /workspace/target/*.jar app.jar
USER app

EXPOSE 8080

# A JVM respeita o limite de memória do contêiner
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"

HEALTHCHECK --interval=15s --timeout=3s --start-period=40s --retries=5 \
  CMD curl -fsS http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
