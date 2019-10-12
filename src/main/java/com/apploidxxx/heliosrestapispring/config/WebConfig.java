package com.apploidxxx.heliosrestapispring.config;

import com.apploidxxx.heliosrestapispring.util.PropertyManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.persistence.EntityManagerFactory;
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
        String username = null;
        String password = null;
        String jdbcUrl = null;
        try {
            username = PropertyManager.getProperty("DB_USERNAME");
            password = PropertyManager.getProperty("DB_PASSWORD");
            jdbcUrl = PropertyManager.getProperty("DB_JDBC_URL");
        } catch (IOException e){
            log.error("Error confused while trying ti load DB credentials", e);
        }

        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setUrl(jdbcUrl);

        return dataSource;
    }

    @Bean
    public JpaTransactionManager transactionManager(EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
