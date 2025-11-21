FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Копируем JAR файл
COPY target/*.jar app.jar

# Указываем порт
EXPOSE 8080

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "/app/app.jar"]