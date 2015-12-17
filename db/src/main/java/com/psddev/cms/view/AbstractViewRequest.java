package com.psddev.cms.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.psddev.dari.util.ObjectUtils;

/**
 * Base implementation of {@link ViewRequest} that handles the creation of views
 * and the conversion of attribute values into their correct return types.
 * Sub-classes must implement {@link #getParameterValue(String)}.
 */
public abstract class AbstractViewRequest implements ViewRequest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractViewRequest.class);

    @Override
    public <V> V createView(Class<V> viewClass, Object model) {

        ViewCreator<Object, V> vc = ViewCreator.createCreator(model, viewClass);

        if (vc != null) {
            return vc.createView(model, this);

        } else {
            LOGGER.warn("Could not find view creator of [" + viewClass
                    + "] for object of type [" + (model != null ? model.getClass() : "null") + "]!");
            return null;
        }
    }

    @Override
    public Object createView(String viewType, Object model) {
        ViewCreator<Object, ?> vc = ViewCreator.createCreator(model, viewType);

        if (vc != null) {
            return vc.createView(model, this);

        } else {
            return null;
        }
    }

    @Override
    public <T> Stream<T> getParameter(Class<T> returnType, String name) {

        Object rawValue = getParameterValue(name);
        if (rawValue instanceof Iterable) {

            List<T> values = new ArrayList<>();

            for (Object rawItem : (Iterable<?>) rawValue) {
                T item = ObjectUtils.to(returnType, rawItem);
                if (item != null) {
                    values.add(item);
                }
            }

            return values.stream();

        } else {
            T value = ObjectUtils.to(returnType, rawValue);
            return value != null ? Collections.singletonList(value).stream() : Stream.empty();
        }
    }

    /**
     * Gets the attribute value with the given {@code name}. If there is more
     * than one value, then an iterable of those values should be returned.
     *
     * @param name the name of the attribute to get.
     * @return the value of the attribute with the given {@code name}.
     */
    protected abstract Object getParameterValue(String name);
}
