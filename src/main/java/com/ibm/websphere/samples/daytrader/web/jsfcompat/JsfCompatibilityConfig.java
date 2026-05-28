package com.ibm.websphere.samples.daytrader.web.jsfcompat;

import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.servlet.ServletContext;

@Configuration(proxyBeanMethods = false)
public class JsfCompatibilityConfig {

    @Bean
    ServletContextInitializer dayTraderFacesContextInitializer() {
        return servletContext -> {
            setInitParameterIfAbsent(servletContext, "jakarta.faces.PROJECT_STAGE", "Production");
            setInitParameterIfAbsent(servletContext, "jakarta.faces.STATE_SAVING_METHOD", "server");
            setInitParameterIfAbsent(servletContext, "jakarta.faces.DEFAULT_SUFFIX", ".xhtml");
            servletContext.setSessionTimeout(30);
        };
    }

    private static void setInitParameterIfAbsent(ServletContext servletContext, String name, String value) {
        if (servletContext.getInitParameter(name) == null) {
            servletContext.setInitParameter(name, value);
        }
    }
}