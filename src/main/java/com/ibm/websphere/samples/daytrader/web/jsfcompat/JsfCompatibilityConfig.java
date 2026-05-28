package com.ibm.websphere.samples.daytrader.web.jsfcompat;

import java.util.List;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ibm.websphere.samples.daytrader.web.filter.JsfCompatibilityLoginFilter;
import com.ibm.websphere.samples.daytrader.web.mvc.CompatibilitySessionFacade;

@Configuration(proxyBeanMethods = false)
public class JsfCompatibilityConfig {

    @Bean
    FilterRegistrationBean<JsfCompatibilityLoginFilter> jsfCompatibilityLoginFilter(CompatibilitySessionFacade sessionFacade) {
        FilterRegistrationBean<JsfCompatibilityLoginFilter> registration = new FilterRegistrationBean<>(
                new JsfCompatibilityLoginFilter(sessionFacade));
        registration.setName("JsfCompatibilityLoginFilter");
        registration.setUrlPatterns(List.of("*.faces", "*.xhtml"));
        registration.setOrder(0);
        return registration;
    }
}