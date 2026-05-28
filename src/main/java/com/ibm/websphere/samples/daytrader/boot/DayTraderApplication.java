package com.ibm.websphere.samples.daytrader.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.ibm.websphere.samples.daytrader")
@EnableScheduling
@ComponentScan(
    basePackages = "com.ibm.websphere.samples.daytrader",
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = "com\\.ibm\\.websphere\\.samples\\.daytrader\\.web\\.(jsf\\..*|prims\\.cdi\\.PingCDIJSFBean)"
    )
)
@EntityScan(basePackages = "com.ibm.websphere.samples.daytrader")
@EnableJpaRepositories(basePackages = "com.ibm.websphere.samples.daytrader.persistence.jpa")
public class DayTraderApplication {

    public static void main(String[] args) {
        SpringApplication.run(DayTraderApplication.class, args);
    }
}