package com.psddev.cms.view;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;

/**
 * The context in which views are created while serving an HTTP request.
 */
public interface ViewRequest {

    /**
     * Creates a view of type {@code viewClass} that is
     * {@link ViewMapping mapped} to the given {@code model}.
     *
     * @param viewClass the view type to create.
     * @param model the model to create the view from.
     * @param <V> the view type to be created.
     * @return the newly created view.
     */
    <V> V createView(Class<V> viewClass, Object model);

    /**
     * Creates a view that is {@link ViewMapping mapped} to
     * the specified {@code viewType} with {@link ViewMapping#types()} for the
     * given {@code model}.
     *
     * @param viewType the type of view to create
     * @param model the model to create the view from.
     * @return the newly created view.
     */
    Object createView(String viewType, Object model);

    /**
     * Gets a stream of the request parameter values for the given {@code name}.
     * A "parameter" is any arbitrary value or attribute of the view request,
     * and is not restricted to just HTTP request parameters. Use
     * {@link #getParameterNames()} to get a list of all the parameters
     * available from the current request.
     *
     * @param returnType the stream type.
     * @param name the parameter name.
     * @param <T> the stream type.
     * @return a stream of the parameter values.
     */
    <T> Stream<T> getParameter(Class<T> returnType, String name);

    /**
     * Gets all the attribute names available to a call to
     * {@link #getParameter(Class, String)}.
     *
     * @return the available parameter names.
     */
    default Set<String> getParameterNames() {
        return Collections.emptySet();
    }
}
