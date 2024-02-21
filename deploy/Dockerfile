FROM openjdk:17-jdk-slim
COPY app.jar /data/app.jar
EXPOSE 8080
CMD ["java","-jar","/data/app.jar","--spring.profiles.active=prod"]
