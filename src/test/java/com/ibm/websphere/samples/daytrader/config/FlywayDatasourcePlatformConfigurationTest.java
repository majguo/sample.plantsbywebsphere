package com.ibm.websphere.samples.daytrader.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class FlywayDatasourcePlatformConfigurationTest {

    @Test
    void detectsDb2FromDatasourceUrl() {
        FlywayDatasourcePlatformConfiguration configuration = new FlywayDatasourcePlatformConfiguration(
            new MockEnvironment().withProperty("spring.datasource.url", "jdbc:db2://127.0.0.1:50000/tradedb")
        );

        assertTrue(configuration.usesDb2());
    }

    @Test
    void appendsDb2FlywayLocationWhenDb2DatasourceIsConfigured() {
        FlywayDatasourcePlatformConfiguration configuration = new FlywayDatasourcePlatformConfiguration(
            new MockEnvironment().withProperty("spring.datasource.driver-class-name", "com.ibm.db2.jcc.DB2Driver")
        );
        FluentConfiguration flyway = new FluentConfiguration().locations(
            "classpath:db/migration/common",
            "classpath:db/migration/shared"
        );

        configuration.dayTraderFlywayPlatformCustomizer().customize(flyway);

        List<String> locations = Arrays.stream(flyway.getLocations())
            .map(location -> location.getDescriptor())
            .toList();
        assertFalse(locations.contains("classpath:db/migration/common"));
        assertTrue(locations.contains("classpath:db/migration/shared"));
        assertTrue(locations.contains("classpath:db/migration/db2"));
    }

    @Test
    void leavesFlywayLocationsUntouchedForNonDb2Datasources() {
        FlywayDatasourcePlatformConfiguration configuration = new FlywayDatasourcePlatformConfiguration(
            new MockEnvironment().withProperty("spring.datasource.url", "jdbc:derby:resources/data/tradedb;create=false")
        );
        FluentConfiguration flyway = new FluentConfiguration().locations(
            "classpath:db/migration/common",
            "classpath:db/migration/shared"
        );

        configuration.dayTraderFlywayPlatformCustomizer().customize(flyway);

        List<String> locations = Arrays.stream(flyway.getLocations())
            .map(location -> location.getDescriptor())
            .toList();
        assertTrue(locations.contains("classpath:db/migration/common"));
        assertTrue(locations.contains("classpath:db/migration/shared"));
        assertFalse(locations.contains("classpath:db/migration/db2"));
    }
}