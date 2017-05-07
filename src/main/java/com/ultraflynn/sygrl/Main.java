/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ultraflynn.sygrl;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.jscience.physics.amount.Amount;
import org.jscience.physics.model.RelativisticModel;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.measure.quantity.Mass;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static javax.measure.unit.SI.KILOGRAM;

@Controller
@SpringBootApplication
public class Main {
    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Autowired
    private DataSource dataSource;

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Main.class, args);
    }

    @RequestMapping("/")
    String index() {
        return "index";
    }

    @RequestMapping("/db")
    String db(Map<String, Object> model) {
        try (Connection connection = dataSource.getConnection()) {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS ticks (tick timestamp)");
            stmt.executeUpdate("INSERT INTO ticks VALUES (now())");
            ResultSet rs = stmt.executeQuery("SELECT tick FROM ticks");

            ArrayList<String> output = new ArrayList<String>();
            while (rs.next()) {
                output.add("Read from DB: " + rs.getTimestamp("tick"));
            }

            model.put("records", output);
            return "db";
        } catch (Exception e) {
            model.put("message", e.getMessage());
            return "error";
        }
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

    @RequestMapping("/hello")
    String hello(Map<String, Object> model) {
        RelativisticModel.select();
        String energy = System.getenv().get("ENERGY");
        if (energy == null) {
            energy = "12 GeV";
        }
        Amount<Mass> m = Amount.valueOf(energy).to(KILOGRAM);
        model.put("science", "E=mc^2: " + energy + " = " + m.toString());
        return "hello";
    }

    @RequestMapping("/callback")
    String callback(Map<String, Object> model, @RequestParam("code") String code, @RequestParam("state") String state) {
        try {
            try (CloseableHttpClient client = HttpClients.createDefault()) {
                HttpPost httpPost = new HttpPost("https://login.eveonline.com/oauth/token");

                String clientId = System.getenv().get("CLIENT_ID");
                String secretKey = System.getenv().get("SECRET_KEY");

                UsernamePasswordCredentials creds
                        = new UsernamePasswordCredentials(clientId, secretKey);
                httpPost.addHeader(new BasicScheme().authenticate(creds, httpPost, null));

                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("grant_type", "authorization_code"));
                params.add(new BasicNameValuePair("code", code));
                httpPost.setEntity(new UrlEncodedFormEntity(params));

                CloseableHttpResponse response = client.execute(httpPost);
                Optional.ofNullable(response.getEntity()).ifPresent(entity -> {
                    JSONObject results = (JSONObject) JSONValue.parse(entity.toString());

                    model.put("code", "code: " + code);
                    model.put("state", "state: " + state);
                    model.put("access_token", "access_token: " + results.get("access_token"));
                    model.put("token_type", "token_type: " + results.get("token_type"));
                    model.put("expires_in", "expires_in: " + results.get("expires_in"));
                    model.put("refresh_token", "refresh_token: " + results.get("refresh_token"));

                    // TODO Now put this is the DB
                });
            }
        } catch (IOException | AuthenticationException e) {
            e.printStackTrace();
        }
        return "callback";
    }

    @RequestMapping("/authorize")
    RedirectView authorize(RedirectAttributes attributes) {
        String clientId = System.getenv().get("CLIENT_ID");
        attributes.addAttribute("response_type", "code");
        attributes.addAttribute("redirect_uri", "https://still-temple-92202.herokuapp.com/callback");
        attributes.addAttribute("client_id", clientId);
        attributes.addAttribute("scope", "publicData characterStatsRead");
        attributes.addAttribute("state", "AAA");
        return new RedirectView("https://login.eveonline.com/oauth/authorize");
    }
}
