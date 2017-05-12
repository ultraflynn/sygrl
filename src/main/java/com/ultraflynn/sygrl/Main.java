package com.ultraflynn.sygrl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.jscience.physics.amount.Amount;
import org.jscience.physics.model.RelativisticModel;
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
    String callback(Map<String, Object> model,
                    @RequestParam("code") String code,
                    @RequestParam("state") String state) {
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

                Optional.ofNullable(client.execute(httpPost).getEntity()).ifPresent(entity -> {
                    try {
                        JsonNode jsonNode = new ObjectMapper().readTree(entity.getContent());

                        model.put("code", "code: " + code);
                        model.put("state", "state: " + state);

                        String accessToken = jsonNode.get("access_token").asText();
                        String tokenType = jsonNode.get("token_type").asText();
                        int expiresIn = jsonNode.get("expires_in").asInt();
                        String refreshToken = jsonNode.get("refresh_token").asText();

                        saveToDb(SaveType.INSERT, model, accessToken, tokenType, expiresIn, refreshToken);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        } catch (IOException | AuthenticationException e) {
            e.printStackTrace();
        }
        return "callback";
    }

    private enum SaveType {
        INSERT, UPDATE
    }

    private void saveToDb(SaveType saveType, Map<String, Object> model, String accessToken, String tokenType,
                          int expiresIn, String refreshToken) {
        try (Connection connection = dataSource.getConnection()) {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS tokens (updated timestamp, access_token text, token_type text, expires_in integer, refresh_token text)");

            Optional<String> sql = Optional.empty();
            if (saveType == SaveType.INSERT) {
                sql = Optional.of("INSERT INTO tokens VALUES (now(), '" + accessToken + "','" + tokenType + "','" + expiresIn + "','" + refreshToken + "')");
            } else if (saveType == SaveType.UPDATE) {
                sql = Optional.of("UPDATE tokens SET updated = now(), access_token = '" + accessToken + "', token_type = '" + tokenType + "', expires_in = '" + expiresIn + "' WHERE refresh_token = '"  + refreshToken + "'");
            }
            sql.ifPresent(s -> {
                try {
                    System.out.println(s);
                    stmt.executeUpdate(s);
                } catch (SQLException e) {
                    model.put("message", e.getMessage());
                    e.printStackTrace();
                }

                // TODO Introduce slf4j/log4j

                model.put("access_token", "access_token: " + accessToken);
                model.put("token_type", "token_type: " + tokenType);
                model.put("expires_in", "expires_in: " + expiresIn);
                model.put("refresh_token", "refresh_token: " + refreshToken);
            });
        } catch (Exception e) {
            model.put("message", e.getMessage());
            e.printStackTrace();
        }
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

    @RequestMapping("/refresh")
    String refresh(Map<String, Object> model) {
        try (Connection connection = dataSource.getConnection()) {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT refresh_token FROM tokens");
            while (rs.next()) {
                refreshToken(model, rs.getString("refresh_token"));
            }
        } catch (Exception e) {
            model.put("message", e.getMessage());
        }
        return "refresh";
    }

    private void refreshToken(Map<String, Object> model, String refreshToken) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost("https://login.eveonline.com/oauth/token");

            String clientId = System.getenv().get("CLIENT_ID");
            String secretKey = System.getenv().get("SECRET_KEY");

            UsernamePasswordCredentials creds
                    = new UsernamePasswordCredentials(clientId, secretKey);
            httpPost.addHeader(new BasicScheme().authenticate(creds, httpPost, null));

            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("grant_type", "refresh_token"));
            params.add(new BasicNameValuePair("refresh_token", refreshToken));
            httpPost.setEntity(new UrlEncodedFormEntity(params));

            Optional.ofNullable(client.execute(httpPost).getEntity()).ifPresent(entity -> {
                try {
                    JsonNode jsonNode = new ObjectMapper().readTree(entity.getContent());

                    String accessToken = jsonNode.get("access_token").asText();
                    String tokenType = jsonNode.get("token_type").asText();
                    int expiresIn = jsonNode.get("expires_in").asInt();
                    String newRefreshToken = jsonNode.get("refresh_token").asText();

                    saveToDb(SaveType.UPDATE, model, accessToken, tokenType, expiresIn, newRefreshToken);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException | AuthenticationException e) {
            e.printStackTrace();
        }
    }
}
