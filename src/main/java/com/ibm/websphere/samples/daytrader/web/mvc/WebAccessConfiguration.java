package com.ibm.websphere.samples.daytrader.web.mvc;

import org.springframework.util.StringUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.ibm.websphere.samples.daytrader.persistence.jpa.AccountProfileJpaRepository;
import com.ibm.websphere.samples.daytrader.persistence.jpa.QuoteJpaRepository;

@Configuration
public class WebAccessConfiguration implements WebMvcConfigurer {

    private static final String BUILD_DB_ACTION = "buildDB";
    private static final String OPERATOR_USER_ID = "uid:0";
    private static final String CANONICAL_QUOTE_ID = "s:1";

    private final CompatibilitySessionFacade sessionFacade;
    private final AccountProfileJpaRepository accountProfileRepository;
    private final QuoteJpaRepository quoteRepository;

    public WebAccessConfiguration(
            CompatibilitySessionFacade sessionFacade,
            AccountProfileJpaRepository accountProfileRepository,
            QuoteJpaRepository quoteRepository) {
        this.sessionFacade = sessionFacade;
        this.accountProfileRepository = accountProfileRepository;
        this.quoteRepository = quoteRepository;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new CompatibilitySessionAccessInterceptor(
                sessionFacade,
                SessionAccessRequirement.OPERATOR,
                this::allowsBootstrapWithoutSession))
                .addPathPatterns("/config");
        registry.addInterceptor(new CompatibilitySessionAccessInterceptor(sessionFacade, SessionAccessRequirement.AUTHENTICATED))
                .addPathPatterns("/rest/quotes/**", "/rest/broadcastevents");
    }

    private boolean allowsBootstrapWithoutSession(jakarta.servlet.http.HttpServletRequest request) {
        return BUILD_DB_ACTION.equals(request.getParameter("action"))
                && !hasCanonicalBootstrapData();
    }

    private boolean hasCanonicalBootstrapData() {
        return accountProfileRepository.existsById(OPERATOR_USER_ID)
                && quoteRepository.existsById(CANONICAL_QUOTE_ID);
    }
}