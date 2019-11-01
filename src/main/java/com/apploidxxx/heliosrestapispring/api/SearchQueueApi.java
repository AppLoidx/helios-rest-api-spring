package com.apploidxxx.heliosrestapispring.api;

import com.apploidxxx.heliosrestapispring.api.model.QueueShortInfo;
import com.apploidxxx.heliosrestapispring.api.util.ErrorResponseFactory;
import com.apploidxxx.heliosrestapispring.entity.Session;
import com.apploidxxx.heliosrestapispring.entity.access.repository.SessionRepository;
import com.apploidxxx.heliosrestapispring.entity.access.repository.queue.QueueRepository;
import com.apploidxxx.heliosrestapispring.entity.queue.Queue;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.PatternSyntaxException;

/**
 * @author Arthur Kupriyanov
 */
@Api("Searching queues")
@RequestMapping("/api/search/queue")
@RestController
public class SearchQueueApi {
    private final QueueRepository queueRepository;
    private final SessionRepository sessionRepository;

    public SearchQueueApi(QueueRepository queueRepository, SessionRepository sessionRepository) {
        this.queueRepository = queueRepository;
        this.sessionRepository = sessionRepository;
    }

    @ApiOperation(value = "Get matching to queue_name queues", response = QueueShortInfo.class, responseContainer = "List")
    @GetMapping(produces = "application/json")
    public Object searchQueue(
            HttpServletResponse response,
            @RequestParam("access_token") String accessToken,

            @ApiParam(value = "short or fullname of queue", required = true)
            @RequestParam("queue_name") String queueName
    ){

        Session session = this.sessionRepository.findByAccessToken(accessToken);
        if (session == null) return ErrorResponseFactory.getInvalidTokenErrorResponse(response);

        return queueMatch(queueName, session);

    }

    private Object queueMatch(String queueName, Session session) {

        List<QueueShortInfo> queueNames = new ArrayList<>();
        for (Queue q : this.queueRepository.findAll()) {
            try {
                if (isMatches(q, queueName)) {
                    queueNames.add(new QueueShortInfo(q, session.getUser()));
                }
            } catch (PatternSyntaxException ignored) { }
        }

        return queueNames;
    }

    private boolean isMatches(Queue q, String matchingWord){
        return q.getName().matches(".*(" + matchingWord + ").*") || q.getFullname().matches(".*(" + matchingWord + ").*");
    }
}
