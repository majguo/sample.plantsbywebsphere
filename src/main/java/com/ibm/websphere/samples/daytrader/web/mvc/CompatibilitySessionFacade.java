package com.ibm.websphere.samples.daytrader.web.mvc;

import java.util.Date;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Component
public class CompatibilitySessionFacade {

    static final String OPERATOR_USER_ID = "uid:0";

    public String getUserId(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object userId = session.getAttribute("uidBean");
        return userId instanceof String ? (String) userId : null;
    }

    public boolean hasAuthenticatedUser(HttpSession session) {
        return getUserId(session) != null;
    }

    public boolean isOperator(HttpSession session) {
        return OPERATOR_USER_ID.equals(getUserId(session));
    }

    public void establishSession(HttpServletRequest request, String userId) {
        HttpSession session = request.getSession(true);
        session.setAttribute("uidBean", userId);
        session.setAttribute("sessionCreationDate", new Date());
    }

    public void invalidateSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }
}