package com.ultraflynn.sygrl.repository;

import com.ultraflynn.sygrl.authentication.User;
import com.ultraflynn.sygrl.repository.PostgresRepository;
import com.ultraflynn.sygrl.repository.Repository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PostgresRepositoryTest {
    private static final LocalDateTime NOW = LocalDateTime.now();

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private Statement statement;

    @Captor
    private ArgumentCaptor<String> sql;

    @Mock
    private User user;

    @Mock
    private ResultSet resultSet;

    private Repository repository;

    @Before
    public void setUp() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        repository = new PostgresRepository(dataSource);
    }

    @Test
    public void shouldInitializeRepository() throws Exception {
        repository.initialize();

        verify(statement, times(2)).executeUpdate(sql.capture());

        assertTrue(sql.getAllValues().get(0).contains("CREATE TABLE"));
        assertTrue(sql.getAllValues().get(1).contains("CREATE UNIQUE INDEX"));
    }

    @Test
    public void shouldSaveNewUser() throws Exception {
        repository.createUser(user);

        verify(statement).executeUpdate(sql.capture());

        assertTrue(sql.getValue().contains("INSERT INTO"));
    }

    @Test
    public void shouldUpdateUser() throws Exception {
        repository.updateUser(user);

        verify(statement).executeUpdate(sql.capture());

        assertTrue(sql.getValue().contains("UPDATE"));
    }

    @Test
    public void shouldRetrieveAllUsers() throws Exception {
        when(statement.executeQuery(anyString())).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        when(resultSet.getInt("character_id")).thenReturn(1219);
        when(resultSet.getString("character_name")).thenReturn("character name");
        when(resultSet.getTimestamp("expires_on")).thenReturn(Timestamp.valueOf(NOW));
        when(resultSet.getString("scopes")).thenReturn("scopes");
        when(resultSet.getString("character_owner_hash")).thenReturn("owner hash");
        when(resultSet.getTimestamp("token_timestamp")).thenReturn(Timestamp.valueOf(NOW));
        when(resultSet.getString("access_token")).thenReturn("access token");
        when(resultSet.getInt("expires_in")).thenReturn(1200);
        when(resultSet.getString("refresh_token")).thenReturn("refresh token");

        List<User> users = repository.getAllUsers();

        verify(statement).executeQuery(sql.capture());

        assertTrue(sql.getValue().contains("SELECT"));
        assertTrue(users.size() == 1);
        assertEquals(1219, users.get(0).getCharacterId());
        assertEquals("character name", users.get(0).getCharacterName());
        assertEquals("scopes", users.get(0).getScopes());
        assertEquals("owner hash", users.get(0).getCharacterOwnerHash());
        assertEquals("access token", users.get(0).getAccessToken());
        assertEquals(1200, users.get(0).getExpiresIn());
        assertEquals("refresh token", users.get(0).getRefreshToken());
    }
}