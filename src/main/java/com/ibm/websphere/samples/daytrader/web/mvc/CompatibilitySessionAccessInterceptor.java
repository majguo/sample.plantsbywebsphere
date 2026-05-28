package com.ibm.websphere.samples.daytrader.web.mvc;

import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CompatibilitySessionAccessInterceptor implements HandlerInterceptor {

    private final CompatibilitySessionFacade sessionFacade;
    private final SessionAccessRequirement requirement;

    public CompatibilitySessionAccessInterceptor(
            CompatibilitySessionFacade sessionFacade,
            SessionAccessRequirement requirement) {
        this.sessionFacade = sessionFacade;
        this.requirement = requirement;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!sessionFacade.hasAuthenticatedUser(request.getSession(false))) {
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