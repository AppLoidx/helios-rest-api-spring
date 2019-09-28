package com.apploidxxx.heliosrestapispring.config;

import com.apploidxxx.heliosrestapispring.util.PropertyManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.sql.DataSource;
import java.io.IOException;

/**
 * @author Arthur Kupriyanov
 */
@Configuration
@EnableWebMvc
@Slf4j
public class WebConfig {
    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();

        dataSource.setDriverClassName("org.postgresql.Driver");
        String username = System.getenv("DB_USERNAME");
        String password = System.getenv("DB_PASSWORD");
        String jdbcUrl = System.getenv("DB_JDBC_URL");
        if (username == null || password == null || jdbcUrl == null) {
            try {
                username = PropertyManager.getProperty("DB_USERNAME");
                password = PropertyManager.getProperty("DB_PASSWORD");
                jdbcUrl = PropertyManager.getProperty("DB_JDBC_URL");
                log.debug("Database username, password and url loaded from local properties");
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        } else {
            log.debug("Database username, password and url loaded from env");
        }


        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setUrl(jdbcUrl);

        return dataSource;
    }
}
