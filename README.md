# Emotipet Backend

EmotiPet é um sistema para auxiliar os donos de pets a analisar as emoções de seus animais por meio de imagens para identificar possíveis sinais de estresse, tristeza ou felicidade, assim permitindo a detecção precoce de problemas de saúde ou comportamentais.

## 🛠️ Tecnologias Utilizadas

- Java 21
- Spring Boot
- Spring Data JPA
- Spring Security
- PostgreSQL
- Docker
- Lombok
- MapStruct (opcional)
- JUnit 5 + Mockito
- JaCoCo (para cobertura de testes)
- PiTest (para testes de mutação)
  
### Pré-requisitos

- Java 21
- Maven 3.8+
- Docker (opcional, para rodar banco de dados)

### ⚙️ Subindo o banco de dados com Docker

```bash
docker-compose up -d
```

### 📄 Documentação da API
A documentação da API está disponível via Swagger em:

```bash
http://localhost:8080/swagger-ui.html
```

### ✅ Testes
```bash
./mvnw clean test 
```

### 🧬 Testes de Mutação com PIT

```bash
./mvnw org.pitest:pitest-maven:mutationCoverage
```

### 📊 Relatório de Cobertura com JaCoCo

```bash
./mvnw clean test jacoco:report

o relatório html será gerado em:
target/site/jacoco/index.html
```






