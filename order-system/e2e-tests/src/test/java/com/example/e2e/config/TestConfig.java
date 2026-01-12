package com.example.e2e.config;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Cucumber Spring configuration for E2E tests.
 */
@CucumberContextConfiguration
@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("test")
public class TestConfig {
}
