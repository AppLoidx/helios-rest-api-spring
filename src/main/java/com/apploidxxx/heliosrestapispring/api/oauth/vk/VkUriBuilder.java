package com.apploidxxx.heliosrestapispring.api.oauth.vk;

import com.apploidxxx.heliosrestapispring.util.PropertyManager;

import java.io.IOException;

/**
 * @author Arthur Kupriyanov
 */
public class VkUriBuilder {

    private static String VK_API_METHOD_URI = "https://api.vk.com/method/";
        private static String VK_USER_INFO_METHOD_URI = VK_API_METHOD_URI + "users.get";
        private final static String clientId ;
        private final static String clientSecret ;
        private final static String redirectUri;

        static {    // initializing vk properties from sys env and local_env.properties
            String clientIdTemp = null;
            String clientSecretTemp = null;
            String redirectUriTemp = null;
            try {
                clientIdTemp = PropertyManager.getProperty("VK_CLIENT_ID");
                clientSecretTemp = PropertyManager.getProperty("VK_CLIENT_SECRET");
                redirectUriTemp = PropertyManager.getProperty("VK_REDIRECT_URI");
            } catch (IOException e) {
                e.printStackTrace();
            }

            clientId = clientIdTemp;
            clientSecret = clientSecretTemp;
            redirectUri = redirectUriTemp;
        }


    /*
        notify - 1
        Добавление ссылки на приложение в меню слева - 256
        offline - 65536
        email - 4194304
     */
        private static String scope = String.valueOf(1 + 256 + 65536 + 4194304);

    private static String version = "5.101";

        public static String getCodeTokenPath(String state) {
            String VK_AUTHORIZE_URI = "https://oauth.vk.com/authorize";
            /*
            page — форма авторизации в отдельном окне;
            popup — всплывающее окно;
         */
            String display = "page";
            String responseType = "code";
            return String.format("%s?client_id=%s" +
                            "&redirect_uri=%s" +
                            "&display=%s" +
                            "&scope=%s" +
                            "&response_type=%s" +
                            "&v=%s" +
                            "&state=%s",
                    VK_AUTHORIZE_URI, clientId, redirectUri, display, scope, responseType, version, state);
        }

        public static String getAccessTokenPath(String code) {
            String VK_ACCESS_URI = "https://oauth.vk.com/access_token";
            return String.format("%s?client_id=%s" +
                            "&client_secret=%s" +
                            "&redirect_uri=%s" +
                            "&code=%s",
                    VK_ACCESS_URI, clientId, clientSecret, redirectUri, code);
        }

        public static String getUserInfoPath(String accessToken, String userId){
            return String.format("%s?users_ids=%s&fields=photo_100,screen_name&access_token=%s&v=%s",
                    VK_USER_INFO_METHOD_URI, userId, accessToken, version);
        }
}
