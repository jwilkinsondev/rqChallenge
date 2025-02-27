package com.reliaquest.api;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class AppConfigTest {
    AppConfig appConfig;

    @BeforeEach
    void setUp() {
        appConfig = new AppConfig();
    }

    @AfterEach
    void tearDown() {
        appConfig = null;
    }

    @Test
    void restTemplate() {
        assertNotNull(appConfig.restTemplate());
    }
}
