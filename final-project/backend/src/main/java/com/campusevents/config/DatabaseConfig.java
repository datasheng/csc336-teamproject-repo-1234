package com.campusevents.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * Database configuration for the Campus Events Platform.
 * 
 * This class configures HikariCP as the connection pool for PostgreSQL.
 * Connection details are read from environment variables for security.
 * 
 * IMPORTANT: This project uses raw SQL with JdbcTemplate.
 * NO ORM (JPA, Hibernate, MyBatis) is used.
 * 
 * Note: This configuration only activates when spring.datasource.driver-class-name
 * is set to PostgreSQL driver (not H2 for tests).
 */
@Configuration
public class DatabaseConfig {

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("${spring.datasource.driver-class-name:org.postgresql.Driver}")
    private String driverClassName;

    /**
     * Creates and configures a HikariCP DataSource.
     * 
     * Pool configuration:
     * - Minimum idle connections: 5
     * - Maximum pool size: 20
     * - Connection timeout: 30 seconds
     * - Idle timeout: 10 minutes
     * - Max lifetime: 30 minutes
     * 
     * This bean only creates when using PostgreSQL driver (production).
     * For tests using H2, Spring Boot's auto-configuration handles the DataSource.
     * 
     * @return Configured HikariDataSource
     */
    @Bean
    @Primary
    @ConditionalOnProperty(name = "spring.datasource.driver-class-name", havingValue = "org.postgresql.Driver")
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        
        // Database connection settings
        config.setJdbcUrl(dbUrl);
        config.setUsername(dbUsername);
        config.setPassword(dbPassword);
        config.setDriverClassName(driverClassName);
        
        // Connection pool settings
        config.setMinimumIdle(5);
        config.setMaximumPoolSize(20);
        config.setConnectionTimeout(30000); // 30 seconds
        config.setIdleTimeout(600000);      // 10 minutes
        config.setMaxLifetime(1800000);     // 30 minutes
        
        // Pool name for identification in logs
        config.setPoolName("CampusEventsHikariCP");
        
        // Additional PostgreSQL-specific settings
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        
        return new HikariDataSource(config);
    }

    /**
     * Creates a JdbcTemplate bean for executing SQL queries.
     * 
     * Use this template for all database operations.
     * Always use parameterized queries to prevent SQL injection.
     * 
     * @param dataSource The configured HikariCP DataSource
     * @return JdbcTemplate instance
     */
    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
