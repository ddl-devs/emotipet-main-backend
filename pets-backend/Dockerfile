FROM eclipse-temurin:21-alpine as build
WORKDIR /workspace/app

# Copiar apenas os arquivos de dependência primeiro
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Baixar dependências
RUN chmod +x ./mvnw
RUN ./mvnw dependency:go-offline -B

# Copiar código fonte e buildar
COPY src src
RUN ./mvnw package -DskipTests

FROM eclipse-temurin:21-alpine
WORKDIR /app
COPY --from=build /workspace/app/target/*.jar app.jar

# Adicionar healthcheck
HEALTHCHECK --interval=30s --timeout=3s \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/swagger-ui/index.html || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]