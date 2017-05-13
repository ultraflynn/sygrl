package com.ultraflynn.sygrl;

import org.junit.Test;

import java.time.LocalDateTime;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;

public class AccessTokenTest {
    @Test
    public void shouldKnowWhenNotExpired() {
        AccessToken accessToken = new AccessToken(LocalDateTime.now(), 1200);
        assertFalse(accessToken.hasExpired());
    }

    @Test
    public void shouldKnowWhenOnExpiryCutover() {
        AccessToken accessToken = new AccessToken(LocalDateTime.now().minusSeconds(1200), 1200);
        assertTrue(accessToken.hasExpired());
    }

    @Test
    public void shouldKnowWhenWellOverExpiry() {
        AccessToken accessToken = new AccessToken(LocalDateTime.now().minusSeconds(1500), 1200);
        assertTrue(accessToken.hasExpired());
    }

    @Test
    public void shouldKnowWhenCloseToExpiry() {
        AccessToken accessToken = new AccessToken(LocalDateTime.now().minusSeconds(1080), 1200);
        assertTrue(accessToken.hasExpired());
    }

    @Test
    public void shouldKnowWhenCloseToExpiryButStillValid() {
        AccessToken accessToken = new AccessToken(LocalDateTime.now().minusSeconds(1079), 1200);
        assertFalse(accessToken.hasExpired());
    }
}