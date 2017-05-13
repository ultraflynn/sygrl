package com.ultraflynn.sygrl;

import com.google.common.collect.ImmutableList;
import com.ultraflynn.sygrl.authentication.AccessToken;
import com.ultraflynn.sygrl.authentication.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;
import java.util.Optional;

public class PostgresRepository implements Repository {
    private static final Logger logger = LoggerFactory.getLogger(PostgresRepository.class);

    private final DataSource dataSource;

    public PostgresRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void initialize() {
        try (Connection connection = dataSource.getConnection()) {
            Statement stmt = connection.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS users (" +
                    "character_id INTEGER, character_name TEXT, expires_on TIMESTAMP, scopes TEXT, " +
                    "character_owner_hash TEXT, token_timestamp TIMESTAMP, access_token TEXT, " +
                    "expires_in INTEGER, refresh_token TEXT, created TIMESTAMP, updated TIMESTAMP)";

            logger.info(sql);
            stmt.executeUpdate(sql);
            stmt.executeUpdate("CREATE UNIQUE INDEX character_id_pk ON users (character_id)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public User createUser(User user) {
        try (Connection connection = dataSource.getConnection()) {
            Statement stmt = connection.createStatement();
            String sql = "INSERT INTO users VALUES (" +
                    "'" + user.getCharacterId() + "', " +
                    "'" + user.getCharacterName() + "', " +
                    "'" + user.getExpiresOn() + "', " +
                    "'" + user.getScopes() + "', " +
                    "'" + user.getCharacterOwnerHash() + "', " +
                    "'" + user.getTokenTimestamp() + "', " +
                    "'" + user.getAccessToken() + "', " +
                    "'" + user.getExpiresIn() + "', " +
                    "'" + user.getRefreshToken() + "', now(), now())";
            logger.info(sql);
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
        return user;
    }

    @Override
    public void updateUser(User user) {
        try (Connection connection = dataSource.getConnection()) {
            Statement stmt = connection.createStatement();
            String sql = "UPDATE users SET " +
                    "expires_on = '" + user.getExpiresOn() + "', " +
                    "scopes = '" + user.getScopes() + "', " +
                    "character_owner_hash = '" + user.getCharacterOwnerHash() + "', " +
                    Optional.ofNullable(user.getTokenTimestamp()).map(dt -> Timestamp.valueOf(dt)).map(t -> "token_timestamp = " + t + ", ").orElse("") +
                    "access_token = '" + user.getAccessToken() + "', " +
                    "expires_in = '" + user.getExpiresIn() + "', " +
                    "updated = now() WHERE character_id = " + user.getCharacterId();
            logger.info(sql);
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<User> getAllUsers() {
        ImmutableList.Builder<User> users = ImmutableList.builder();

        try (Connection connection = dataSource.getConnection()) {
            Statement stmt = connection.createStatement();
            String sql = "SELECT character_id, character_name, expires_on, scopes, " +
                    "character_owner_hash, token_timestamp, access_token, expires_in, refresh_token FROM users";
            logger.info(sql);
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                AccessToken accessToken = new AccessToken(rs.getTimestamp("token_timestamp").toLocalDateTime(),
                        rs.getInt("expires_in"), rs.getString("access_token"),
                        rs.getString("refresh_token"));

                User user = new User(accessToken, rs.getInt("character_id"),
                        rs.getString("character_name"),
                        rs.getTimestamp("expires_on").toLocalDateTime(),
                        rs.getString("scopes"), rs.getString("character_owner_hash"));
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users.build();
    }
}
