package com.ibm.websphere.samples.daytrader.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication(scanBasePackages = "com.ibm.websphere.samples.daytrader")
@ComponentScan(
    basePackages = "com.ibm.websphere.samples.daytrader",
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = "com\\.ibm\\.websphere\\.samples\\.daytrader\\.web\\.jsf\\..*"
    )
)
public class DayTraderApplication {

    public static void main(String[] args) {
        SpringApplication.run(DayTraderApplication.class, args);
    }
}