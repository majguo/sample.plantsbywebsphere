package com.ibm.websphere.samples.daytrader.web.mvc;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebAccessConfiguration implements WebMvcConfigurer {

    private final CompatibilitySessionFacade sessionFacade;

    public WebAccessConfiguration(CompatibilitySessionFacade sessionFacade) {
        this.sessionFacade = sessionFacade;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new CompatibilitySessionAccessInterceptor(sessionFacade, SessionAccessRequirement.OPERATOR))
                .addPathPatterns("/config");
        registry.addInterceptor(new CompatibilitySessionAccessInterceptor(sessionFacade, SessionAccessRequirement.AUTHENTICATED))
                .addPathPatterns("/rest/quotes/**", "/rest/broadcastevents");
    }
}