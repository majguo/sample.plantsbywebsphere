package com.ibm.websphere.samples.daytrader.web.jsfcompat;

import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

abstract class JsfFacesSupport {

    protected ExternalContext externalContext() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext == null) {
            throw new IllegalStateException("No active FacesContext");
        }
        return facesContext.getExternalContext();
    }

    protected HttpServletRequest request() {
        return (HttpServletRequest) externalContext().getRequest();
    }

    protected HttpSession session(boolean create) {
        return (HttpSession) externalContext().getSession(create);
    }
}