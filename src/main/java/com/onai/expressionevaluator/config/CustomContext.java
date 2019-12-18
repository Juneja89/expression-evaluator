package com.onai.expressionevaluator.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContext;

@Component
public class CustomContext {

    @Autowired
    private ServletContext servletContext;

    public void setAttribute(String key, Object value) {
        servletContext.setAttribute(key,value);
    }

    public Object getAttribute(String key) {
        return servletContext.getAttribute(key);
    }
}