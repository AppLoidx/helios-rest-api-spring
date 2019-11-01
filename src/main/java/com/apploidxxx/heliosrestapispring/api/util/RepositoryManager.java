package com.apploidxxx.heliosrestapispring.api.util;

import com.apploidxxx.heliosrestapispring.api.exception.persistence.EntityNotFoundException;
import com.apploidxxx.heliosrestapispring.api.exception.persistence.InvalidAccessTokenException;
import com.apploidxxx.heliosrestapispring.api.exception.persistence.PersistenceException;
import com.apploidxxx.heliosrestapispring.entity.Session;
import com.apploidxxx.heliosrestapispring.entity.access.repository.SessionRepository;
import com.apploidxxx.heliosrestapispring.entity.access.repository.UserRepository;
import com.apploidxxx.heliosrestapispring.entity.access.repository.queue.QueueRepository;
import com.apploidxxx.heliosrestapispring.entity.queue.Queue;
import com.apploidxxx.heliosrestapispring.entity.user.User;
import org.springframework.stereotype.Component;

/**
 * @author Arthur Kupriyanov
 */
@Component
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

    public QueueFind getQueue() throws PersistenceException {
        return new QueueFind();
    }

    class UserFind{

        public User byUsername(String username){
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

    class QueueFind{
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