package com.psddev.cms.view;

import com.psddev.cms.db.PageStage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

/**
 * ViewContext implementation that uses the Java Servlet Spec for handling HTTP
 * requests.
 */
public class ServletViewContext implements ViewContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServletViewContext.class);

    private HttpServletRequest request;

    public ServletViewContext(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public <V> V createView(Class<V> viewClass, Object model) {

        ViewCreator<Object, V> vc = ViewCreator.findCreator(model, viewClass);

        if (vc != null) {
            return vc.createView(model, this);

        } else {
            LOGGER.warn("Could not find view creator of [" + viewClass
                    + "] for object of type [" + model.getClass() + "]!");
            return null;
        }
    }

    @Override
    public PageStage getPageStage() {
        return (PageStage) request.getAttribute("stage");
    }

    @Override
    public String getParameter(String name) {
        return request.getParameter(name);
    }

    @Override
    public List<String> getParameters(String name) {
        return Arrays.asList(request.getParameterValues(name));
    }

    @Override
    public String getHeader(String name) {
        return request.getHeader(name);
    }

    @Override
    public List<String> getHeaders(String name) {
        return Collections.list(request.getHeaders(name));
    }
}
