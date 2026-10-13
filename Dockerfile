# ---------- Etapa 1: build ----------
# Imagem com Maven + JDK 21 só para compilar e empacotar o .jar
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Baixa as dependências primeiro: enquanto o pom.xml não muda, essa camada fica em cache
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
# Os testes rodam no pipeline (etapa de testes); aqui só empacotamos
RUN mvn -B -q package -DskipTests

# ---------- Etapa 2: runtime ----------
# Imagem final enxuta: só o JRE e o .jar
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Usuário sem privilégios de root
RUN addgroup -S homa && adduser -S homa -G homa
COPY --from=build /app/target/homa.jar app.jar
USER homa

# Commit gravado na imagem pelo pipeline; aparece em /api/info
ARG GIT_COMMIT=desconhecido
ENV GIT_COMMIT=${GIT_COMMIT} \
    PORT=8080 \
    JAVA_OPTS="-XX:MaxRAMPercentage=75"
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:${PORT}/actuator/health | grep -q UP || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
