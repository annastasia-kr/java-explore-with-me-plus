FROM openjdk:11-jre-slim

WORKDIR /app

# Копируем JAR файл
COPY target/*.jar app.jar

# Указываем порт
EXPOSE 8080

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "/app/app.jar"]