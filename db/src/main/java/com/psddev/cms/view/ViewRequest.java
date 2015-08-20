package com.psddev.cms.view;

import java.util.stream.Stream;

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
     * Gets a stream of the HTTP request parameter values for the given {@code name}.
     *
     * @param returnType the stream type.
     * @param name the parameter name.
     * @param <T> the stream type.
     * @return a stream of the parameter values.
     */
    <T> Stream<T> getParameter(Class<T> returnType, String name);

    /**
     * Gets a stream of the HTTP request header values for the given {@code name}.
     *
     * @param returnType the stream type.
     * @param name the parameter name.
     * @param <T> the stream type.
     * @return a stream of the header values.
     */
    <T> Stream<T> getHeader(Class<T> returnType, String name);

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
