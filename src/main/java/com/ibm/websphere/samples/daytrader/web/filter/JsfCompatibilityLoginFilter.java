package com.ibm.websphere.samples.daytrader.web.filter;

import java.io.IOException;

import org.springframework.web.filter.OncePerRequestFilter;

import com.ibm.websphere.samples.daytrader.web.mvc.CompatibilitySessionFacade;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JsfCompatibilityLoginFilter extends OncePerRequestFilter {

    private final CompatibilitySessionFacade sessionFacade;

    public JsfCompatibilityLoginFilter(CompatibilitySessionFacade sessionFacade) {
        this.sessionFacade = sessionFacade;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String userId = sessionFacade.getUserId(request.getSession(false));
        String url = request.getServletPath();
        if (userId == null) {
            if (url.contains("home") || url.contains("account") || url.contains("portfolio") || url.contains("quote")
                    || url.contains("order") || url.contains("marketSummary")) {
                response.sendRedirect("welcome.faces");
                return;
            }
        }

        if (url.endsWith(".faces")) {
            String xhtmlPath = url.substring(0, url.length() - ".faces".length()) + ".xhtml";
            if (request.getServletContext().getResource(xhtmlPath) != null) {
                request.getRequestDispatcher(xhtmlPath).forward(request, response);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}