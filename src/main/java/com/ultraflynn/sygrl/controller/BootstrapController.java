package com.ultraflynn.sygrl.controller;

import com.ultraflynn.sygrl.authentication.EveOnlineSSO;
import com.ultraflynn.sygrl.authentication.SSOAuthenticator;
import com.ultraflynn.sygrl.authentication.UserRegistry;
import com.ultraflynn.sygrl.repository.PostgresRepository;
import com.ultraflynn.sygrl.repository.Repository;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.sql.DataSource;
import java.sql.SQLException;

@Controller
public class BootstrapController {
    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Autowired
    private Repository repository;

    @Bean
    public Repository repository() throws SQLException {
        return new PostgresRepository(dataSource());
    }

    @Bean
    public DataSource dataSource() throws SQLException {
        if (dbUrl == null || dbUrl.isEmpty()) {
            return new HikariDataSource();
        } else {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(dbUrl);
            return new HikariDataSource(config);
        }
    }

    @Bean
    public UserRegistry userRegistry() throws SQLException {
        return new UserRegistry(ssoAuthenticator(), repository());
    }

    @Bean
    public SSOAuthenticator ssoAuthenticator() {
        return new EveOnlineSSO();
    }

    @RequestMapping("/initialize")
    String initialize() {
        repository.initialize();
        return "initialize";
    }
}
