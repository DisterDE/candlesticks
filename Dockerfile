FROM gradle:jdk21 AS build

WORKDIR /app

COPY . .

RUN gradle build

FROM openjdk:21-jdk-slim

WORKDIR /opt/app

COPY --from=build /app/build/libs/candlesticks-all.jar .

EXPOSE 9000

CMD ["java", "-jar", "candlesticks-all.jar"]