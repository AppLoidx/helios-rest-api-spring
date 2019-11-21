# CommandChain
### Использование
Иногда требуются ендпоинты, которые в одном методе могут исполнять различные операции.
Например, `GroupsControlApi`. Туда передаются параметры `property`, который опеределяет,
какой именно метод должен вызваться, и `value` какое-то значение.

Первое, что приходит в голову - это создание `switch-case`:
```java
switch (property){
    case "method1":
        return doMethodOne(value);
        break;
    case "method2":
        return doMethodTwo(value);
        break;
    ...
    default:
        return defaultMethod(value);
}
```

Но при создании новых методов нам нужно будет совершить много операций: 
создать метод, добавить новый кейс, отправить туда параметры.

Альтернативным решением будет создание отдельного map-контейнера, который будет хранить
команды.

```java
private class CommandOne implements Action {
    public String execute(String value){
        return "some execution result";
    }   
}
```

При этом, следует учитывать, что передаваемых параметров в метод `execute` может быть несколько.
Так как в каждом контроллере использоуется различный контекст. Например, туда могут передать пользователя
совершившего запрос, или сущность очереди, группы.

Встает вопрос о гибкости такого решения. Если мы каждый раз будем прописывать все это вручную в `PostConstruct`, то
мы получим весьма непрактичное решение.

Тогда давайте делегируем создание контейнера какому-нибудь отдельному классу. А за реализацию этого класса, будет
отвечать сам программист. То есть, мы должны будем создать класс, который соберет все эти команды и вернет Map.

Так как мы хотим, чтобы реализация этой команды была под контролем программиста, используются аннотация `@Command`,
которая избавляет программиста от имплементации какого-нибудь интерфейса.

Наша команда превращается в:
```java
@Command("method1")
private class CommandOne implements Action {
    public String execute(String value){
        return "some execution result of method 1";
    }   
}

@Command("method2")
private class CommandTwo implements Action {
    public String execute(String value){
        return "some execution result of method 2";
    }   
}

private interface Action {
    String execute(String value);
}
```

Тогда задача стоит в том, чтобы разработать класс, который все внутренние классы помеченные `@Command` и сгенерирует
`Map`, в ключах которых будет хранится имя метода.

Так как все операции по работе с этой картой сводятся к тому, что мы вызываем `get(String key)`, то мы можем сделать
обертку. Такой оберткой послужил класс `Chain`

Он позволяет вызвать `getAction()`, который работает также как `get()` в `Map`, но при отсутсвии соответствующего
действия - генерирует `ActionNotFoundException`, который наследуется от `ResponsibleException` (то есть перехватывается `ExceptionHandler`)

Рассмотрим использование на примере:
```java
@Slf4j
@RestController
@RequestMapping("/api/groups.control/{groupName}")
public class GroupsControlApi {

    private final GroupRepository groupRepository;
    private final RepositoryManager repositoryManager;

    public GroupsControlApi(GroupRepository groupRepository, RepositoryManager repositoryManager) {
        this.groupRepository = groupRepository;
        this.repositoryManager = repositoryManager;
    }

    private Chain<Action> chain;
    // создаем цепочку наших команд
    
    @PostConstruct
    private void init(){
        chain = new CommandChain().init(this, Action.class);
        // инициализируем передав ссылку на этот контроллер и наш интерфейс (метку типа)
    }

    @PutMapping
    public Object putSetting(
            HttpServletResponse response,

            @PathVariable("groupName") String groupName,

            @RequestParam("access_token") String accessToken,

            @RequestParam("property") String property,
            @RequestParam("value") String value
    ){

        UsersGroup group = this.groupRepository.findByName(groupName);
        User user = repositoryManager.getUser().byAccessToken(accessToken);
        if (group == null) return ErrorResponseFactory.getInvalidParamErrorResponse("group not found", response);

        property = property.toLowerCase();

        return chain.getAction(property, response).execute(value, group, user, response);
        // получаем нужную команды и вызываем у него метод исполнения с нужными параметрами.
    }
    
    // определяем интерфейс, который будут реализовывать команды
    
    @FunctionalInterface
    private interface Action{
        Object execute(String value, UsersGroup usersGroup, User user, HttpServletResponse response);
    }

    // определяем команды
    // @Command(имя_команды)

    @Command("add_user")
    private class AddUserCommand implements Action {

        @Override
        public Object execute(String value, UsersGroup usersGroup, User user, HttpServletResponse response) {
            // some action ...
            return null;
        }
    }

    @Command("remove_user")
    private class RemoveUserCommand implements Action {

        @Override
        public Object execute(String value, UsersGroup usersGroup, User user, HttpServletResponse response) {
            // some action ...
            return null;
        }
    }

    @Command("change_password")
    private class ChangePasswordCommand implements Action {

        @Override
        public Object execute(String value, UsersGroup usersGroup, User user, HttpServletResponse response) {
            // some action ...
            return null;
        }
    }

    @Command("change_fullname")
    private class ChangeFullnameCommand implements Action {

        @Override
        public Object execute(String value, UsersGroup usersGroup, User user, HttpServletResponse response) {
            // some action ...
            return null;
        }
    }
   
}
``` 


