# Helios REST API [Spring Edition]

Переписанная на Spring🍃 версия [Helios API](https://github.com/AppLoidx/helios-rest-api) со стека Java EE 🏭

## Важно☠️
Тесты не написаны, поэтому не факт, что проект рабочий, но базовый тест `contextLoads` проходит

## Maven завимимости вне экосистемы Spring 👀
```xml
    <dependencies>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>

        <dependency>
            <groupId>commons-codec</groupId>
            <artifactId>commons-codec</artifactId>
            <version>1.9</version>
        </dependency><!-- MD5Crypt for encrypting passwords-->

        <dependency>
            <groupId>com.google.api-client</groupId>
            <artifactId>google-api-client</artifactId>
            <version>1.30.2</version>
        </dependency><!-- Google OAuth -->

    </dependencies>
```
