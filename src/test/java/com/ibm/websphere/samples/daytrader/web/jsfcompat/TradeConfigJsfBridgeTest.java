package com.ibm.websphere.samples.daytrader.web.jsfcompat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

import org.junit.jupiter.api.Test;

class TradeConfigJsfBridgeTest {

    @Test
    void exposesAWritableMaxQuotesBeanPropertyForJsfBinding() throws IntrospectionException {
        PropertyDescriptor maxQuotes = findProperty("maxQuotes");

        assertNotNull(maxQuotes.getWriteMethod());
        assertEquals("setMaxQuotes", maxQuotes.getWriteMethod().getName());
    }

    private PropertyDescriptor findProperty(String propertyName) throws IntrospectionException {
        for (PropertyDescriptor descriptor : Introspector.getBeanInfo(TradeConfigJsfBridge.class).getPropertyDescriptors()) {
            if (propertyName.equals(descriptor.getName())) {
                return descriptor;
            }
        }
        throw new AssertionError("Missing property descriptor for " + propertyName);
    }
}