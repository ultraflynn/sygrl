package com.ultraflynn.sygrl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserRegistryTest {
    @Mock
    private Repository repository;

    @Mock
    private Authenticator authenticator;

    @Test
    public void shouldAddNewUser() {
        User user = new User();
        when(authenticator.requestNewUser()).thenReturn(user);

        UserRegistry registry = new UserRegistry(repository, authenticator);
        registry.addNewUser();

        verify(repository).saveUser(user);
    }
}
