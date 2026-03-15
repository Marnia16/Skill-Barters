package com.skillbarter.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * CONCEPT 12: Spring Java-based configuration.
 * Scans com.skillbarter.* for @Service, @Repository, @Component beans.
 */
@Configuration
@ComponentScan(basePackages = "com.skillbarter")
@EnableTransactionManagement
public class AppConfig {
    // Spring automatically finds all @Service and @Repository
    // classes under com.skillbarter and wires them together.
}
