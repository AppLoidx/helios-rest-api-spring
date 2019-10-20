package com.apploidxxx.heliosrestapispring.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * @author Arthur Kupriyanov
 */
@Configuration
public class ObjectMapperConfiguration implements WebMvcConfigurer {

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {

        // configuration for correctly displaying POJO props in snake_case in Swagger

        ObjectMapper objectMapper = null;
        for (HttpMessageConverter converter : converters) {
            if (converter instanceof MappingJackson2HttpMessageConverter ) {
                MappingJackson2HttpMessageConverter jacksonConverter =
                        ((MappingJackson2HttpMessageConverter) converter);

                if (objectMapper == null) {
                    objectMapper = jacksonConverter.getObjectMapper();
                } else {
                    jacksonConverter.setObjectMapper(objectMapper);
                }
            }
        }
    }
}
