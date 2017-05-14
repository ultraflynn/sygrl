package com.ultraflynn.sygrl.authentication;

import com.google.common.collect.ImmutableList;
import com.ultraflynn.sygrl.Repository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UserRegistryTest {
    @Mock
    private SSOAuthenticator authenticator;

    @Mock
    private Repository repository;

    @Mock
    private AccessToken expiredToken;

    @Mock
    private AccessToken validToken;

    @Mock
    private AccessToken newToken;

    @Captor
    private ArgumentCaptor<User> updatedUser;

    private UserRegistry registry;

    @Before
    public void setUp() {
        registry = new UserRegistry(authenticator, repository);
    }

    @Test
    public void shouldAddNewUser() {
        AccessToken accessToken = new AccessToken(LocalDateTime.now(), 1200, "access token", "refresh token");
        User authenticatedUser = new User(accessToken, 364, "name", "scopes", "owner hash");

        when(authenticator.requestState()).thenReturn("state");
        when(authenticator.requestAccessToken("code", "state")).thenReturn(accessToken);
        when(authenticator.requestCharacterInfo(accessToken)).thenReturn(authenticatedUser);
        when(repository.createUser(authenticatedUser)).thenReturn(authenticatedUser);

        User user = registry.addNewUser("code", "state");

        verify(repository).createUser(authenticatedUser);

        assertEquals(user, authenticatedUser);
    }

    @Test
    public void shouldRevalidateAllRelevantTokens() {
        when(expiredToken.hasExpired()).thenReturn(true);
        when(validToken.hasExpired()).thenReturn(false);

        User userOne = new User(expiredToken, 364, "name", "scopes", "owner hash");
        User userTwo = new User(validToken, 365, "name", "scopes", "owner hash");

        List<User> users = ImmutableList.of(userOne, userTwo);
        when(repository.getAllUsers()).thenReturn(users);
        when(authenticator.revalidateToken(expiredToken)).thenReturn(newToken);

        registry.revalidateAllTokens();

        verify(authenticator).revalidateToken(expiredToken);
        verify(repository).updateUser(updatedUser.capture());
        verifyNoMoreInteractions(authenticator);

        assertEquals(newToken, updatedUser.getValue().getToken());
    }
}
