# Отлавливание исключений и их разработка

## Исключения для отлавливания Spring

Думаю, нет смысла оговаривать обычные исключения, но есть некоторые согласования, которых стоит придерживаться:

1. Все исключения должны храниться в `com.apploidxxx.heliosrestapispring.api.exception`.
2. Исключения (без необходимости) не должны заполнять стек-трейс

Теперь, приступим к рассмотрению исключений, которые может ловить сам Spring и отправлять клиенту

Для начала посмотрим абстрактный супер класс всех исключений, которые ловит Spring

```java
public abstract class ResponsibleException extends RuntimeException{
    public ResponsibleException(){
        this("responsible exception");
    }
    public ResponsibleException(String message){
        super(message, new RuntimeException(), false, false);
    }

    public ResponsibleException(String message, Throwable t, boolean enableSuppression, boolean writableStackTrace){
        super(message, t, enableSuppression, writableStackTrace);
    }

    public abstract ErrorMessage getResponse(HttpServletResponse response);
}
```

Обрабатывается эта ошибка здесь:
```java
@ControllerAdvice
public class ExceptionHandler {

    @org.springframework.web.bind.annotation.ExceptionHandler(ResponsibleException.class)
    public ErrorMessage handleException(HttpServletResponse response, ResponsibleException e){
        return e.getResponse(response);
    }
}
```

Рекомендуется воздержаться от использования такого метода, так как это замедляет
работу программы и вы передаете контроль коду, который может отправить не тот ответ клиенту.

Лучшей практикой будет использовальние `ErrorResponseFactory`.

## ErrorResponseFactory

Вы также можете использовать фабрику собщений об ошибках, который возвратит клиенту специальный json вида:
```json
{
    "error" : "error_name",
    "error_description" : "description of error"    
}
```

`error` - короткая классификация ошибки, `error_description` - более полное описание

Пример использования:

```java
if (isAuthorized){
    response.setStatus(HttpServletResponse.SC_OK);
} else {
    return ErrorResponseFactory.getUnauthorizedErrorResponse("unauthorized", "your token is expired or invalid", response);
}
```

## PersistenceException

Это исключение используется при операциях с репозиториями сущностей. 

Пример использования

```java
public class RepositoryManager {
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final QueueRepository queueRepository;

    public RepositoryManager(UserRepository userRepository, SessionRepository sessionRepository, QueueRepository queueRepository){
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.queueRepository = queueRepository;
    }


    public UserFind getUser() throws PersistenceException {
        return new UserFind();
    }

    public boolean isUserExist(String username){
        return this.userRepository.findByUsername(username) != null;
    }

    public void saveUser(User user){
        this.userRepository.save(user);
    }
    public void saveQueue(Queue queue) { this.queueRepository.save(queue); }

    public QueueFind getQueue() throws PersistenceException {
        return new QueueFind();
    }

    public void deleteQueue(List<Queue> list) {
        this.queueRepository.deleteInBatch(list);
    }

    public class UserFind{

        public User byUsername(String username) throws EntityNotFoundException{
            User user = userRepository.findByUsername(username);
            checkEntityIsNotNull(user);
            return user;
        }

        public User byAccessToken(String accessToken) throws PersistenceException {
            Session s = sessionRepository.findByAccessToken(accessToken);
            if (s == null) throw new InvalidAccessTokenException();
            User user = s.getUser();
            checkEntityIsNotNull(user);
            return user;
        }
    }

    public class QueueFind{
        public Queue byQueueName(String queueName){
            Queue queue = queueRepository.findByName(queueName);
            checkEntityIsNotNull(queue);
            return queue;
        }
    }

    private void checkEntityIsNotNull(Object o) throws EntityNotFoundException{
        if (o == null) throw new EntityNotFoundException();
    }
}
```