package com.ibm.websphere.samples.daytrader.entities;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AccountProfileDataBeanTest {

    @Test
    void redactsSecretsFromRenderedRepresentations() {
        AccountProfileDataBean profile = new AccountProfileDataBean(
                "uid:1",
                "secret",
                "User One",
                "addr",
                "mail@example.com",
                "4111111111111111");

        String plainText = profile.toString();
        String html = profile.toHTML();

        assertFalse(plainText.contains("secret"));
        assertFalse(plainText.contains("4111111111111111"));
        assertFalse(html.contains("secret"));
        assertFalse(html.contains("4111111111111111"));
        assertTrue(plainText.contains("[PROTECTED]"));
        assertTrue(html.contains("****-****-****-1111"));
    }
}