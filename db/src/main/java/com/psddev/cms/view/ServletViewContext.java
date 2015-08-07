package com.psddev.cms.view;

import com.psddev.cms.db.PageStage;
import com.psddev.dari.util.TypeDefinition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

public class ServletViewContext implements ViewContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServletViewContext.class);

    private HttpServletRequest request;

    public ServletViewContext(HttpServletRequest request) {
        this.request = request;
    }

    public void initializeView(Object view) {
        HttpRequest.applyServletHttpRequest(request, view);
        applyPageStage(view);
    }

    @Override
    public <V> V createView(Class<V> viewClass, Object model) {

        ViewCreator<Object, V> vc = ViewCreator.findCreator(model, viewClass);

        if (vc != null) {
            V view = vc.createView(model, this);
            initializeView(view);
            return view;

        } else {
            LOGGER.warn("Could not find view creator of [" + viewClass
                    + "] for object of type [" + model.getClass() + "]!");
            return null;
        }
    }

    private void applyPageStage(Object view) {
        Class<?> viewClass = view.getClass();

        TypeDefinition.getInstance(viewClass).getAllFields().forEach((field) -> {
            if (field.isAnnotationPresent(Cms.PageStage.class) && field.getType().isAssignableFrom(PageStage.class)) {
                try {
                    field.set(view, request.getAttribute("stage"));
                } catch (IllegalAccessException e) {
                    // do nothing
                }
            }
        });
    }
}
