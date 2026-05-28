package com.ibm.websphere.samples.daytrader.web.jsfcompat;

import java.io.Serializable;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

@Component("pingCDIJSFBean")
@SessionScope
public class PingCdiJsfBridge implements Serializable {

    private static final long serialVersionUID = -7475815494313679416L;

    private int hitCount;

    public int getHitCount() {
        return ++hitCount;
    }
}