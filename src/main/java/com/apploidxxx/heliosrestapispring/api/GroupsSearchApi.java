package com.apploidxxx.heliosrestapispring.api;

import com.apploidxxx.heliosrestapispring.api.exception.persistence.InvalidAccessTokenException;
import com.apploidxxx.heliosrestapispring.api.model.GroupModel;
import com.apploidxxx.heliosrestapispring.entity.access.repository.SessionRepository;
import com.apploidxxx.heliosrestapispring.entity.access.repository.group.GroupRepository;
import com.apploidxxx.heliosrestapispring.entity.group.UsersGroup;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Arthur Kupriyanov
 */
@RestController
@RequestMapping("/api/groups.search")
public class GroupsSearchApi {

    private final SessionRepository sessionRepository;
    private final GroupRepository groupRepository;

    public GroupsSearchApi(SessionRepository sessionRepository, GroupRepository groupRepository) {
        this.sessionRepository = sessionRepository;
        this.groupRepository = groupRepository;
    }

    @GetMapping
    public List<GroupModel> searchGroups(
            @RequestParam("access_token") String accessToken,
            @RequestParam("query") final String query
    ){

        // TODO: write TEST

        if (sessionRepository.findByAccessToken(accessToken) == null)
            throw new InvalidAccessTokenException();

        List<UsersGroup> allGroups = groupRepository.findAll();

        return allGroups.stream()
                .filter(group -> group.getName().matches(".*" + query + ".*") || group.getFullname().matches(".*" + query + ".*"))
                .map(GroupModel::new)
                .collect(Collectors.toList());

    }

}
