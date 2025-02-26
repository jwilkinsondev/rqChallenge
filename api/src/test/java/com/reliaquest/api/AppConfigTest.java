package com.reliaquest.api;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
