package com.ibm.websphere.samples.daytrader.config;

import java.util.ArrayList;
import java.util.List;

import org.flywaydb.core.api.Location;
import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration(proxyBeanMethods = false)
class FlywayDatasourcePlatformConfiguration {

    private static final String COMMON_LOCATION = "classpath:db/migration/common";
    private static final String SHARED_LOCATION = "classpath:db/migration/shared";
    private static final String DB2_LOCATION = "classpath:db/migration/db2";
    private static final String DB2_URL_PREFIX = "jdbc:db2:";
    private static final String DB2_DRIVER = "com.ibm.db2.jcc.DB2Driver";

    private final Environment environment;

    FlywayDatasourcePlatformConfiguration(Environment environment) {
        this.environment = environment;
    }

    @Bean
    FlywayConfigurationCustomizer dayTraderFlywayPlatformCustomizer() {
        return configuration -> {
            if (!usesDb2()) {
                return;
            }
            List<String> locations = currentLocations(configuration.getLocations());
            replaceBaselineLocation(locations);
            ensureLocation(locations, SHARED_LOCATION);
            ensureLocation(locations, DB2_LOCATION);
            configuration.locations(locations.toArray(String[]::new));
        };
    }

    boolean usesDb2() {
        return usesDb2(
            environment.getProperty("spring.datasource.url"),
            environment.getProperty("spring.datasource.driver-class-name")
        );
    }

    boolean usesDb2(String datasourceUrl, String driverClassName) {
        return matchesDb2(datasourceUrl) || DB2_DRIVER.equals(driverClassName);
    }

    private static boolean matchesDb2(String datasourceUrl) {
        return datasourceUrl != null && datasourceUrl.startsWith(DB2_URL_PREFIX);
    }

    private static List<String> currentLocations(Location[] configuredLocations) {
        List<String> locations = new ArrayList<>();
        if (configuredLocations == null || configuredLocations.length == 0) {
            locations.add(COMMON_LOCATION);
            locations.add(SHARED_LOCATION);
            return locations;
        }
        for (Location location : configuredLocations) {
            locations.add(location.getDescriptor());
        }
        return locations;
    }

    private static void replaceBaselineLocation(List<String> locations) {
        int commonLocationIndex = locations.indexOf(COMMON_LOCATION);
        if (commonLocationIndex >= 0) {
            locations.set(commonLocationIndex, DB2_LOCATION);
            return;
        }
        ensureLocation(locations, DB2_LOCATION);
    }

    private static void ensureLocation(List<String> locations, String location) {
        if (!locations.contains(location)) {
            locations.add(location);
        }
    }
}