# 构建阶段
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY settings.xml /usr/share/maven/ref/settings.xml
COPY pom.xml .
RUN mvn -s /usr/share/maven/ref/settings.xml dependency:go-offline
COPY src ./src
RUN mvn -s /usr/share/maven/ref/settings.xml package -DskipTests

# 运行阶段
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENV TZ=Asia/Shanghai
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
