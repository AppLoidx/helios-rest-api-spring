# Getting Started

### Конфигурация

* Java 1.8
* Spring Boot 2.1.8
* Default port: 3000
* Database platform: PostgreSQL
* Default JDBC URL: `jdbc:postgresql://localhost:5432/postgres`
* Default username: `postgres`
* Default password: `postgres`

Также вы можете настроить конфигурацию через переменные системного окружения:

* Port: `PORT`
* JDBC URL: `DB_JDBC_URL`
* username: `DB_USERNAME`
* password: `DB_PASSWORD`

> **Note**:  Значение из переменной окружения приоритетнее, чем значение по умолчанию. В частности, если присутствует значение в окружении, то будет использоваться оно, в другом же случае, значение по умолчанию

### Сборка

1. **Сборка проекта**

   ```cmd
    mvn clean install
   ```

2. **Запуск приложения**

   ```
   java -jar target\helios-rest-api-spring-<version>-SNAPSHOT.jar
   ```

   Вместо `<version>` указать версию проекта

3. **Обращение к API**

   1. Чтобы узнать какие методы есть, откройте страницу 

       ```
        http://localhost:3000/swagger-ui.html#/ 
       ```

         где вместо 3000 может быть указано значение из переменной `PORT` окружения
       
   2. Чтобы сделать запрос, можете воспользоваться интерфейсом на той же странице swagger или сделать запрос через `curl`
   
       ```bash
       curl -X GET "localhost:3000/api/auth?login=123&password=123
       ```
   
       ```json
       {
           "error_message":"invalid_credentials",
        	"error_description":"invalid login or password"
       }
       ```
   
       

### Тестирование

* Default port: 3000 (`PORT`)
* Database platform: H2 in memory
* Data Source URL: `jdbc:h2:mem:db;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=TRUE`
* Default username: `sa`
* Default password: `sa`

```
mvn test
```

Для изменения конфигураций тестирования, крому порта, необходимо изменить файл конфигурации приложения для тестирования `test/resources/application.properties`