package com.ultraflynn.sygrl.authentication;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class EveOnlineSSO implements SSOAuthenticator {
    private static final Logger logger = LoggerFactory.getLogger(EveOnlineSSO.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Function<HttpEntity, AccessToken> generateAccessToken = entity -> {
        try {
            JsonNode jsonNode = objectMapper.readTree(entity.getContent());
            logger.info("Response {}", jsonNode);

            String accessToken = jsonNode.get("access_token").asText();
            int expiresIn = jsonNode.get("expires_in").asInt();
            String refreshToken = jsonNode.get("refresh_token").asText();

            return new AccessToken(LocalDateTime.now(), expiresIn, accessToken, refreshToken);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    };

    @Override
    public String requestState() {
        return "sygrl"; // TODO This should generate a strong key and validate it in requestAccessToken()
    }

    @Override
    public AccessToken requestAccessToken(String code, String state) {
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

            return Optional.ofNullable(client.execute(httpPost).getEntity())
                    .map(generateAccessToken)
                    .orElseThrow(() -> new RuntimeException("Failed to obtain access token"));
        } catch (IOException | AuthenticationException e) {
            logger.error("Error requesting access token", e);
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    @Override
    public User requestCharacterInfo(AccessToken accessToken) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost("https://login.eveonline.com/oauth/verify");

            String clientId = System.getenv().get("CLIENT_ID");
            String secretKey = System.getenv().get("SECRET_KEY");

            UsernamePasswordCredentials creds
                    = new UsernamePasswordCredentials(clientId, secretKey);
            httpPost.addHeader(new BasicScheme().authenticate(creds, httpPost, null));

            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("token_type", "Bearer"));
            params.add(new BasicNameValuePair("access_token", accessToken.getAccessToken()));
            httpPost.setEntity(new UrlEncodedFormEntity(params));

            return Optional.ofNullable(client.execute(httpPost).getEntity())
                    .map(entity -> {
                        try {
                            JsonNode jsonNode = objectMapper.readTree(entity.getContent());
                            logger.info("Response {}", jsonNode);

                            int characterId = jsonNode.get("CharacterID").asInt();
                            String characterName = jsonNode.get("CharacterName").asText();
                            LocalDateTime expiresOn = LocalDateTime.parse(jsonNode.get("ExpiresOn").asText());
                            String scopes = jsonNode.get("Scopes").asText();
                            String characterOwnerHash = jsonNode.get("CharacterOwnerHash").asText();

                            return new User(accessToken, characterId, characterName, expiresOn, scopes, characterOwnerHash);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .orElseThrow(() -> new RuntimeException("Failed to obtain access token"));
        } catch (IOException | AuthenticationException e) {
            logger.error("Error requesting access token", e);
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    @Override
    public AccessToken revalidateToken(AccessToken accessToken) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost("https://login.eveonline.com/oauth/token");

            String clientId = System.getenv().get("CLIENT_ID");
            String secretKey = System.getenv().get("SECRET_KEY");

            UsernamePasswordCredentials creds
                    = new UsernamePasswordCredentials(clientId, secretKey);
            httpPost.addHeader(new BasicScheme().authenticate(creds, httpPost, null));

            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("grant_type", "refresh_token"));
            params.add(new BasicNameValuePair("refresh_token", accessToken.getRefreshToken()));
            httpPost.setEntity(new UrlEncodedFormEntity(params));

            return Optional.ofNullable(client.execute(httpPost).getEntity())
                    .map(generateAccessToken)
                    .orElseThrow(() -> new RuntimeException("Failed to revalidate token " + accessToken));
        } catch (IOException | AuthenticationException e) {
            logger.error("Error revalidating access token", e);
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
