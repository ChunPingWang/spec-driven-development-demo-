package com.example.e2e.steps;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Cucumber Spring configuration for E2E tests.
 * This class must be in the glue path for Cucumber-Spring integration to work.
 */
@CucumberContextConfiguration
@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("test")
public class CucumberSpringConfiguration {
}
