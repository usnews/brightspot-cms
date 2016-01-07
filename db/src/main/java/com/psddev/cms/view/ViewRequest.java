package com.psddev.cms.view;

import java.util.stream.Stream;

/**
 * @deprecated no replacement.
 */
@Deprecated
public interface ViewRequest {

    /**
     * @deprecated Use {@link com.psddev.cms.view.ViewCreator#findCreatorClass(Object, Class)}
     * with {@link com.psddev.cms.view.ViewCreator#createCreator(Class)} instead.
     */
    @Deprecated
    default <V> V createView(Class<V> viewClass, Object model) {
        return null;
    }

    /**
     * @deprecated Use {@link com.psddev.cms.view.ViewCreator#findCreatorClass(Object, String)}
     * with {@link com.psddev.cms.view.ViewCreator#createCreator(Class)} instead.
     */
    @Deprecated
    default Object createView(String viewType, Object model) {
        return null;
    }

    /**
     * @deprecated Create a new class and annotate its fields with
     * {@link com.psddev.cms.view.servlet.HttpParameter}.
     */
    @Deprecated
    default <T> Stream<T> getParameter(Class<T> returnType, String name) {
        return null;
    }
}
