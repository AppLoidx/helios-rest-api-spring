# Тестирование

## Конфигурация

При тестировании Spring подтягивает отдельный `application.properties` из папки ресурсов тестирования

При тестировании используется in-memory бд H2

Дефолтная конфигурация:
```properties
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.url=jdbc:h2:mem:db;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=TRUE
spring.datasource.username=sa
spring.datasource.password=sa
```

Также конфигуарции бд и моделей:
```properties
spring.jackson.property-naming-strategy=SNAKE_CASE

spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.jdbc.lob.non_contextual_creation=true
spring.jpa.open-in-view=true
spring.jpa.hibernate.naming.physical-strategy=org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl
```

## Общий принцип тестирования

Для каждого контроллера или же тестируемого класса создается класс тестирования с эквивалентным именем
но только с приставкой `Test` в конце. При этом расположение (иерархия) директорий такая же, как и у
тестируемых контроллеров. Это помогает лучше ориентироваться в программе.

Также могут присутствовать тесты для util-классов для тестирования. Именование такое же, но они должны располагаться
в той же директории, что и тестируемый класс. Например, можно посмотреть реализацию `MockUtil`, расположенный
в `api/testutil`.

Все util-классы для процесса тестирования должны быть расположены в пакете `testutil` (`util` содержит тестируемые классы
программы).

## Тестирование контроллеров

Тестирование контроллеров выполняется через MockMvc. Пример его внедрения для тестирования контроллера `AuthApi` :

```java
    @Autowired
    private AuthApi authApiController;

    private MockMvc mockMvc;

    @Before
    public void init() {
        this.mockMvc = standaloneSetup(this.authApiController).build();
    }
```

Зачастую, в контроллерах нужно использовать каких-то пользователей (клиентов) с токенами и прочим. Для их манипуляции
существует класс `MockUtil`, который может создавать уже `mocked` клиентов.

Например,

```java
    @Test
    public void fail_auth() throws Exception {

        User user = mockUtil.getRandomUserWithMockedRepository();

        mockMvc.perform(get("/api/auth")
                .param("login", user.getUsername())
                .param("password", "another password"))
                .andExpect(status().isUnauthorized());
    }
```

Здесь следует быть внимательным с тем, что вы не можете сделать инъекцию `MockBean` для некоторых репозиториев, так
как они уже внедрены в `MockUtil`. Если вам нужен mock bean репозитория, то вам нужно поулчить его через геттер:

```java
when(mockUtil.getUserRepositoryMockBean().findByUsername(user.getUsername())).thenReturn(user);
```

В этом примере мы получили `UserRepository` из `mockUtil`.

### UserBuilder

Для создания пользователей можно воспользоваться билдером.

Пример использования:
```java
User teacher = UserBuilder.createUser().withUsertype(UserType.TEACHER).build();

User actualUser = UserBuilder.createUser().withName("1").withPassword("2").withFirstName("3").withLastName("4").withEmail("5").build();
```