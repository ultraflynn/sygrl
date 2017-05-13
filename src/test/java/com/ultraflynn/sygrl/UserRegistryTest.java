package com.ultraflynn.sygrl;

import com.google.common.collect.ImmutableList;
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
        AuthorizationCode authorizationCode = new AuthorizationCode();
        AccessToken accessToken = new AccessToken(LocalDateTime.now(), 1200);
        User authenticatedUser = new User(accessToken);

        when(authenticator.requestAuthorizationCode()).thenReturn(authorizationCode);
        when(authenticator.requestAccessToken(authorizationCode)).thenReturn(accessToken);
        when(authenticator.requestCharacterInfo(accessToken)).thenReturn(authenticatedUser);

        User user = registry.addNewUser();

        verify(repository).saveUser(authenticatedUser);

        assertEquals(user, authenticatedUser);
    }

    @Test
    public void shouldRevalidateAllRelevantTokens() {
        when(expiredToken.hasExpired()).thenReturn(true);
        when(validToken.hasExpired()).thenReturn(false);

        User userOne = new User(expiredToken);
        User userTwo = new User(validToken);

        List<User> users = ImmutableList.of(userOne, userTwo);
        when(repository.getAllUsers()).thenReturn(users);
        when(authenticator.revalidateToken(expiredToken)).thenReturn(newToken);

        registry.revalidateAllTokens();

        verify(authenticator).revalidateToken(expiredToken);
        verify(repository).saveUser(updatedUser.capture());
        verifyNoMoreInteractions(authenticator);

        assertEquals(newToken, updatedUser.getValue().getToken());
    }
}
