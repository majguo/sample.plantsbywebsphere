package com.ibm.websphere.samples.daytrader.web.mvc;

import java.util.function.Predicate;

import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CompatibilitySessionAccessInterceptor implements HandlerInterceptor {

    private final CompatibilitySessionFacade sessionFacade;
    private final SessionAccessRequirement requirement;
    private final Predicate<HttpServletRequest> anonymousAllowance;

    public CompatibilitySessionAccessInterceptor(
            CompatibilitySessionFacade sessionFacade,
            SessionAccessRequirement requirement) {
        this(sessionFacade, requirement, request -> false);
    }

    public CompatibilitySessionAccessInterceptor(
            CompatibilitySessionFacade sessionFacade,
            SessionAccessRequirement requirement,
            Predicate<HttpServletRequest> anonymousAllowance) {
        this.sessionFacade = sessionFacade;
        this.requirement = requirement;
        this.anonymousAllowance = anonymousAllowance;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!sessionFacade.hasAuthenticatedUser(request.getSession(false))) {
            if (anonymousAllowance.test(request)) {
                return true;
            }
            response.sendError(HttpStatus.UNAUTHORIZED.value());
            return false;
        }
        if (requirement == SessionAccessRequirement.OPERATOR
                && !sessionFacade.isOperator(request.getSession(false))) {
            response.sendError(HttpStatus.FORBIDDEN.value());
            return false;
        }
        return true;
    }
}