package com.psddev.cms.view;

import com.psddev.cms.db.PageStage;

import java.util.List;

/**
 * The context in which views are created while serving an HTTP request.
 */
public interface ViewRequest {

    /**
     * Creates a view of the specified {@code viewClass} based on the given
     * {@code model}.
     *
     * @param viewClass the view type to create.
     * @param model the model to create the view from.
     * @param <V> the view type to be created.
     * @return the created view.
     */
    <V> V createView(Class<V> viewClass, Object model);

    /**
     * Gets the PageStage object in context of the current HTTP request.
     *
     * @return the page stage of the current HTTP request.
     */
    PageStage getPageStage();

    /**
     * Gets the value of the HTTP request parameter for the given {@code name}.
     *
     * @param name the parameter name.
     * @return the value of the request parameter.
     */
    String getParameter(String name);

    /**
     * Gets the list of values of the HTTP request parameter for the given
     * {@code name}.
     *
     * @param name the parameter name.
     * @return the list of request parameter values.
     */
    List<String> getParameters(String name);

    /**
     * Gets the value of the HTTP request header for the given {@code name}.
     *
     * @param name the header name.
     * @return the value of the request header.
     */
    String getHeader(String name);

    /**
     * Gets the list of values of the HTTP request header for the given
     * {@code name}.
     *
     * @param name the header name.
     * @return the list of request header values.
     */
    List<String> getHeaders(String name);

    /**
     * Gets the layout view class for the given {@code model} based on the
     * {@link com.psddev.cms.view.LayoutView} annotation.
     *
     * @param model the model to check.
     * @return the layout view class
     */
    default Class<?> getLayoutViewClass(Object model) {
        LayoutView annotation = model.getClass().getAnnotation(LayoutView.class);
        return annotation != null ? annotation.value() : null;
    }

    /**
     * Gets the main view class for the given {@code model} based on the
     * {@link com.psddev.cms.view.MainView} annotation.
     *
     * @param model the model to check.
     * @return the main view class.
     */
    default Class<?> getMainViewClass(Object model) {
        MainView annotation = model.getClass().getAnnotation(MainView.class);
        return annotation != null ? annotation.value() : null;
    }
}
