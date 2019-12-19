# Введение в разработку

Инструкции по разработке и дополнении проекта.

## Общая картина

Весь проект можно условно разделить на две части.

1. REST API  - общедоступный API для управления очередями
2. Backend - одна из возможных реализаций этого API

В этом проекте рассматривается первый из них, интерфейс для манипуляции очередями. А реализациями могут случить как отдельные веб-приложения, как и просто приложения или даже боты.

## Окружение

Сервис расположен на платформе (PaaS) Heroku

<h2 align=center><img src="https://i.imgur.com/naipRUa.png " /> </h2>

 

Разворачивается по запросу, а не по веб-хукам из гит репозитория (Automation deploy)

## Spring - это наше все

Как было указано в [Getting Started](../GettingStarted.md) в проекте используется версия Spring Boot 2.1.8

Конфигурирование ендпоинтов через JavaConfig или `application.properties`

Пример:

```java
@RequestMapping("/api/badges")
@RestController
public class BadgesApi {    

    @PutMapping
    public Object putBadge(
        HttpServletResponse response,

        @RequestParam("username") String username,
        @RequestParam("access_token") String accessToken,

        @ApiParam("Badge's name like developer, teacher and etc.")
        @RequestParam("badge_name") String badgeName

    ){ ... }
}
```

Для генерирования автоматической документации используется Swagger, а именно:

```xml
<!-- https://mvnrepository.com/artifact/io.springfox/springfox-swagger2 -->
<dependency>
    <groupId>io.springfox</groupId>
    <artifactId>springfox-swagger2</artifactId>
    <version>2.9.2</version>
</dependency>

<!-- https://mvnrepository.com/artifact/io.springfox/springfox-swagger-ui -->
<dependency>
    <groupId>io.springfox</groupId>
    <artifactId>springfox-swagger-ui</artifactId>
    <version>2.9.2</version>
</dependency>
```

Настраиваться документация через аннотации. [Подробнее]( https://www.vojtechruzicka.com/documenting-spring-boot-rest-api-swagger-springfox/ )

## Иерархия директорий

### Общий вид

|         Директория          | Назначение                                                   |
| :-------------------------: | ------------------------------------------------------------ |
|           `/api`            | Здесь находятся ендпоинты с маппингом, а также вспомогательным Utility-классы |
|          `/config`          | Конфигурации развертывания Spring-приложения                 |
|          `/entity`          | Сущности для JPA. Некоторые разделены тематически по папкам, если сущностей > 1 |
| `/entity/access/repository` | Репозитории для доступа к сущностям из БД. DAO.              |
|           `util`            | Utility-классы для всего приложения. Например, работа с переменными окружения |

### Директория /api

Внутреннее содержание папки `api`

|  Директория  | Назначение                                                   |
| :----------: | ------------------------------------------------------------ |
|     `.`      | В самой директории находятся ендпоинты с приставкой в конце `Api` |
| `/exception` | Кастомные исключения, созданные для отлавливания в ExceptionHandler |
|  `/filter`   | Фильтры приложения. Там же находиться ExceptionHandler       |
|   `/model`   | Модели обмена данными, если отправка обычных сущностей недостаточна или избыточна |
|   `/oauth`   | Все, что связано с OAuth 2.0 авторизацией через другие сервисы. Важно заметить, что реализация OAuth самого приложения для других - расположена не здесь |
|   `/util`    | Utility-классы для ендпоинтов и вспомогательных им классах   |



Теперь, когда у вас есть общее представление о приложении, углубимся в каждый его модуль

* [Создание и отлавливание исключений](ExceptionHandle.md)
* [Разработка API](Endpoints.md)
* [Углубляемся в реализацию OAuth 2.0](OAuth.md)
* [Работа с сущностями и базой данных](Database.md)
* [Тестирование](Test.md)