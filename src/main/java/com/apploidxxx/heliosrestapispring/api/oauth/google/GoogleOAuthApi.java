package com.apploidxxx.heliosrestapispring.api.oauth.google;


import com.apploidxxx.heliosrestapispring.util.PropertyManager;
import com.apploidxxx.heliosrestapispring.api.model.AuthorizationCodeModel;
import com.apploidxxx.heliosrestapispring.entity.AuthorizationCode;
import com.apploidxxx.heliosrestapispring.entity.user.User;
import com.apploidxxx.heliosrestapispring.entity.access.repository.AuthorizationCodeRepository;
import com.apploidxxx.heliosrestapispring.entity.access.repository.ContactDetailsRepository;
import com.apploidxxx.heliosrestapispring.entity.access.repository.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

/**
 * @author Arthur Kupriyanov
 */
@Controller
@RequestMapping(value = "/api/google/oauth")
public class GoogleOAuthApi {
    private final AuthorizationCodeRepository authorizationCodeRepository;
    private final UserRepository userRepository;
    private final ContactDetailsRepository contactDetailsRepository;
    public GoogleOAuthApi(AuthorizationCodeRepository authorizationCodeRepository, UserRepository userRepository, ContactDetailsRepository contactDetailsRepository) {
        this.authorizationCodeRepository =  authorizationCodeRepository;
        this.userRepository = userRepository;
        this.contactDetailsRepository = contactDetailsRepository;
    }

    @PostMapping(produces = "application/json")
    public @ResponseBody AuthorizationCodeModel handleRequest(
            HttpServletResponse response,
            @RequestHeader("X-Requested-With") String requestedWith,
            @RequestParam("code") String code) throws URISyntaxException, IOException {

        // protect against CSRF attacks
        if (!requestedWith.equals("XMLHttpRequest")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }

        final String REDIRECT_URI = PropertyManager.getProperty("GOOGLE_REDIRECT_URI");

        GoogleTokenResponse tokenResponse = createGoogleTokenResponse(code, REDIRECT_URI);

        GoogleIdToken.Payload payload = tokenResponse.parseIdToken().getPayload();

        final String EMAIL = payload.getEmail();
        final String PICTURE = (String) payload.get("picture");
        final String FAMILY_NAME = (String) payload.get("family_name");
        final String GIVEN_NAME = (String) payload.get("given_name");

        User user = this.contactDetailsRepository.findByEmail(EMAIL).getUser();

        if (user == null) user = createNewUser(EMAIL, GIVEN_NAME, FAMILY_NAME, EMAIL, PICTURE);

        response.setStatus(HttpServletResponse.SC_OK);
        return saveAuthorizationCodeAndGetModel(new AuthorizationCode(user));


    }

    private User createNewUser(String username, String firstName, String lastName, String email, String pictureUrl){
        User user = new User(username, null, firstName, lastName, email);
        user.getContactDetails().setImg(pictureUrl);
        this.userRepository.save(user);
        return user;
    }

    private AuthorizationCodeModel saveAuthorizationCodeAndGetModel(AuthorizationCode authorizationCode){
        this.authorizationCodeRepository.save(authorizationCode);
        return new AuthorizationCodeModel(authorizationCode.getAuthCode());
    }

    private URL getGoogleClientSecretFileUrl() throws IOException {
        return Objects.requireNonNull(GoogleOAuthApi.class.getClassLoader().getResource(PropertyManager.getProperty("GOOGLE_CLIENT_SECRET_FILE_PATH")), "[NPE] Can't find google client secret");
    }

    private GoogleTokenResponse createGoogleTokenResponse(String code, String redirectUri) throws IOException, URISyntaxException {
        GoogleClientSecrets clientSecrets = getGoogleClientSecrets();
        return new GoogleAuthorizationCodeTokenRequest(
                new NetHttpTransport(),
                JacksonFactory.getDefaultInstance(),
                "https://www.googleapis.com/oauth2/v4/token",
                clientSecrets.getDetails().getClientId(),
                clientSecrets.getDetails().getClientSecret(),
                code,
                redirectUri)
                .execute();
    }

    private GoogleClientSecrets getGoogleClientSecrets() throws URISyntaxException, IOException {
        return GoogleClientSecrets.load(
                JacksonFactory.getDefaultInstance(), new FileReader(new File(getGoogleClientSecretFileUrl().toURI())));
    }
}
