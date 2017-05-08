package com.ultraflynn.sygrl;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

                Optional.ofNullable(client.execute(httpPost).getEntity()).ifPresent(entity -> {
                    ObjectMapper mapper = new ObjectMapper().setVisibility(PropertyAccessor.CREATOR, JsonAutoDetect.Visibility.ANY);
                    String json;
                    try {
                        json = inputStreamToString(entity.getContent());
                    } catch (IOException e) {
                        json = e.getMessage();
                    }
                    System.out.println("JSON: ^" + json + "^");
                    AccessToken accessToken = mapper.convertValue(json, AccessToken.class);

                    model.put("code", "code: " + code);
                    model.put("state", "state: " + state);
                    model.put("access_token", "access_token: " + accessToken.getAccessToken());
                    model.put("token_type", "token_type: " + accessToken.getTokenType());
                    model.put("expires_in", "expires_in: " + accessToken.getExpiresIn());
                    model.put("refresh_token", "refresh_token: " + accessToken.getRefreshToken());

                    // TODO Now put this is the DB
                });
            }
        } catch (IOException | AuthenticationException e) {
            e.printStackTrace();
        }
        return "callback";
    }

    private String inputStreamToString(InputStream stream) throws IOException {
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(stream))) {
            return buffer.lines().collect(Collectors.joining("\n"));
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
}
