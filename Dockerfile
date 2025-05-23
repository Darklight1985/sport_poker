FROM gradle:8.14.0-jdk21
RUN mkdir sport_poker
WORKDIR /sport_poker
COPY . .
RUN gradle clean build -x test
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "build/libs/sport-poker-1.0.0.jar"]