package com.apploidxxx.heliosrestapispring.api.oauth.vk;

import com.apploidxxx.heliosrestapispring.api.oauth.vk.model.AccessToken;

import com.apploidxxx.heliosrestapispring.api.oauth.vk.model.UserInfo;
import com.apploidxxx.heliosrestapispring.api.oauth.vk.model.VkUser;
import com.apploidxxx.heliosrestapispring.entity.AuthorizationCode;
import com.apploidxxx.heliosrestapispring.entity.ContactDetails;
import com.apploidxxx.heliosrestapispring.entity.user.User;
import com.apploidxxx.heliosrestapispring.entity.access.repository.AuthorizationCodeRepository;
import com.apploidxxx.heliosrestapispring.entity.access.repository.ContactDetailsRepository;
import com.apploidxxx.heliosrestapispring.entity.access.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;

import java.io.IOException;


/**
 * @author Arthur Kupriyanov
 */
@Controller
@RequestMapping(value = "/api/vk/oauth")
@Slf4j
public class VkOAuthApi {
    private final ContactDetailsRepository contactDetailsRepository;
    private final AuthorizationCodeRepository authorizationCodeRepository;
    private final UserRepository userRepository;
    public VkOAuthApi(ContactDetailsRepository contactDetailsRepository, AuthorizationCodeRepository authorizationCodeRepository, UserRepository userRepository) {
        this.contactDetailsRepository = contactDetailsRepository;
        this.authorizationCodeRepository = authorizationCodeRepository;
        this.userRepository = userRepository;
    }

    @RequestMapping(value = "/redirect")
    @GetMapping
    public Object getRedirectUri(HttpServletResponse response, @RequestParam("redirect_uri") String redirectUri) throws IOException {
        response.sendRedirect(VkUriBuilder.getCodeTokenPath(redirectUri));
        return null;
    }

    @GetMapping
    public Object redirectService(
            HttpServletResponse response,
            @RequestParam(value = "code", required = false) String accessCode,
            @RequestParam(value = "state", required = false) String stateRedirectUrl,
            @RequestParam(value = "error", required = false) String error) throws  IOException {

        if (accessCode == null){
            if ("access_denied".equals(error)){
                response.sendRedirect("/html/external/login.html");
            }
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return null;
        }

        AccessToken token = new RestTemplate().getForEntity(VkUriBuilder.getAccessTokenPath(accessCode), AccessToken.class).getBody();

        if (token == null || token.getAccessToken() == null){
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return null;
        }


        ContactDetails contactDetails = this.contactDetailsRepository.findByVkontakteId(Long.valueOf(token.getUserId()));
        if (contactDetails != null) {
            User heliosUser = contactDetails.getUser();
            return redirectWithAuthCode(heliosUser, stateRedirectUrl, response);
        }

        if (token.getEmail() != null) {
            contactDetails = this.contactDetailsRepository.findByEmail(token.getEmail());
            if (contactDetails != null) {
                User heliosUser = contactDetails.getUser();
                heliosUser.getContactDetails().setVkontakteId(Long.parseLong(token.getUserId()));
                this.userRepository.save(heliosUser);
                return redirectWithAuthCode(heliosUser, stateRedirectUrl, response);

            }
        }

        UserInfo user = new RestTemplate().getForEntity(VkUriBuilder.getUserInfoPath(token.getAccessToken(), token.getUserId()), UserInfo.class).getBody();
        if (user == null) {
            response.setStatus(500);
            log.error("Error while trying get response vk user info");
            return null;
        }

        // vk method users.get returns a list of users
        VkUser vkUser = user.getResponse().get(0);

        if (vkUser.getScreenName() == null) {
            vkUser.setScreenName("vk" + vkUser.getId());
        }
        User heliosUser = setUpUser(vkUser, token.getEmail());
        this.userRepository.save(heliosUser);

        return redirectWithAuthCode(heliosUser, stateRedirectUrl, response);
    }

    private Object redirectWithAuthCode(User user, String redirectUrl, HttpServletResponse response) throws IOException {
        AuthorizationCode authorizationCode = new AuthorizationCode(user);
        this.authorizationCodeRepository.save(authorizationCode);
        response.sendRedirect(redirectUrl + "?authorization_code=" + authorizationCode.getAuthCode());
        return null;
    }

    private User setUpUser(VkUser vkUser, String email){
        if (vkUser.getScreenName() == null) {
            vkUser.setScreenName("vk" + vkUser.getId());
        }
        User user = new User(vkUser.getScreenName(), null, vkUser.getFirstName(), vkUser.getLastName());
        if (email != null) user.getContactDetails().setEmail(email);
        user.getContactDetails().setImg(vkUser.getPhoto100Url());
        user.getContactDetails().setVkontakteId(Long.valueOf(vkUser.getId()));

        return user;
    }

}
