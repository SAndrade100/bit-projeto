# ---- build ----
FROM maven:3-eclipse-temurin-25 AS build
WORKDIR /app

# Camada separada para as dependências: só é refeita quando o pom.xml muda.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q -DskipTests package

# ---- runtime ----
FROM eclipse-temurin:25-jre
WORKDIR /app

RUN useradd --system --uid 10001 appuser
COPY --from=build /app/target/solicitacoes-*.jar app.jar
USER appuser

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
