package com.psddev.cms.view;

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
     * @deprecated Use {@link #getParameter(Class, String, String)} instead.
     */
    @Deprecated
    default <T> Stream<T> getParameter(Class<T> returnType, String name) {
        return getParameter(returnType, null, name);
    }

    /**
     * Gets a stream of parameter values for the given {@code namespace} and
     * {@code name}. A "parameter" is any arbitrary value or attribute of the
     * view request, and is not restricted to just HTTP request parameters. Use
     * {@link #getParameterNamespaces()} to get a list of all the namespaces
     * containing parameters that are available on the current request, and use
     * {@link #getParameterNames(String)} to get a list of all the parameter
     * names available within a particular namespace for the current request.
     *
     * @param returnType the stream type.
     * @param namespace the namespace the parameter lives in.
     * @param name the parameter name.
     * @param <T> the stream type.
     * @return a stream of the parameter values.
     */
    <T> Stream<T> getParameter(Class<T> returnType, String namespace, String name);

    /**
     * Gets all the parameter names available within the given {@code namespace}.
     *
     * @return the available parameter names.
     */
    Set<String> getParameterNames(String namespace);

    /**
     * Gets all the available parameter namespaces.
     *
     * @return the available parameter namespaces.
     */
    Set<String> getParameterNamespaces();
}
