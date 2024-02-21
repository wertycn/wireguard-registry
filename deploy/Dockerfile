FROM openjdk:17-jdk-slim
COPY app.jar /data/app.jar
EXPOSE 5480
CMD ["java","-jar","/data/app.jar","--spring.profiles.active=prod"]
