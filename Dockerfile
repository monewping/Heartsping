# 빌드 환경
FROM amazoncorretto:17 AS builder

WORKDIR /app

COPY build.gradle settings.gradle gradlew ./
COPY gradle gradle
RUN chmod +x ./gradlew \
    && ./gradlew --no-daemon dependencies

COPY src src
RUN ./gradlew --no-daemon bootJar -x test

# 실행 환경
FROM amazoncorretto:17

ENV PROJECT_NAME=monewping \
    PROJECT_VERSION=0.0.1 \
    JVM_OPTS=""

RUN yum update -y \
    && yum install -y curl \
    && yum clean all

WORKDIR /app

COPY --from=builder /app/build/libs/${PROJECT_NAME}-${PROJECT_VERSION}-SNAPSHOT.jar app.jar

COPY --from=builder /app/src/main/resources/static /app/static

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JVM_OPTS -jar ./app.jar"]