## "Мессенджер"&nbsp;&mdash; REST веб-сервис

### Запуск сервиса:

Подразумевается, что текущая рабочая директория&nbsp;&mdash; корневая директория
проекта.

*1.* Предварительно поднять контейнеризированную СУБД PostgreSQL.

Пример команды для Docker для поднятия временного контейнера для разработки и
ручного тестирования (для автоматизированного интеграционного тестирования не
требуется ввиду поднятия СУБД в коде с помощью *Testcontainers*):

`docker run --rm --name dev-postgres --publish 5432:5432 -e POSTGRES_PASSWORD=postgres -d postgres`

Останов контейнера удалит его:

`docker stop dev-postgres`

*2.* Сборка исполняемого "fat"-JAR, используя Maven-Wrapper

`./mvnw clean package spring-boot:repackage`

*3.* Запуск JAR

`java -jar ./target/spring-messenger-server-0.0.1-SNAPSHOT.jar`


### Структура приложения:

![Структура приложения](https://raw.githubusercontent.com/alexeycoder/illustrations/main/java-gb-diplom/server-prj-structure.png)


### OpenAPI/Swagger:

![OpenAPI/Swagger](https://raw.githubusercontent.com/alexeycoder/illustrations/main/java-gb-diplom/swagger_ui_full.png) 


### Интеграционное тестирование End-points:

Запуск, используя Maven-Wrapper: `./mvnw test`

Пример запуска в Eclipse IDE:

![Тестирование](https://raw.githubusercontent.com/alexeycoder/illustrations/main/java-gb-diplom/rest-service-tests-1.png)
