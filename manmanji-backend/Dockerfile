FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src

# 自动生成 JWT RSA 密钥（每次构建生成全新的，避免 gitignore 遗留问题）
RUN mkdir -p src/main/resources/keys && \
    openssl genrsa -out src/main/resources/keys/rsa_private.pem 2048 && \
    openssl rsa -in src/main/resources/keys/rsa_private.pem -pubout -out src/main/resources/keys/rsa_public.pem

RUN mvn package -DskipTests -q

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/manmanji-backend-1.0-SNAPSHOT.jar app.jar

# 上传文件存储目录
RUN mkdir -p /data
VOLUME /data

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
