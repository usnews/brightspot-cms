package com.psddev.cms.view;

import com.psddev.dari.db.Location;
import com.psddev.dari.db.Metric;
import com.psddev.dari.db.Recordable;
import com.psddev.dari.db.State;
import com.psddev.dari.util.Once;
import com.psddev.dari.util.StringUtils;
import com.psddev.dari.util.TypeDefinition;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ViewMap implements Map<String, Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ViewMap.class);

    private Map<String, Supplier<Object>> unresolved;

    private Map<String, Object> resolved;

    private Object view;

    private Once resolver = new Once() {
        @Override
        protected void run() throws Exception {
            ViewMap.this.keySet().forEach(ViewMap.this::get);
        }
    };

    public ViewMap(Object view) {
        this.view = view;
        this.unresolved = new HashMap<>();
        this.resolved = new HashMap<>();

        // TODO: Switch to using java bean spec
        TypeDefinition typeDef = TypeDefinition.getInstance(view.getClass());
        Map<String, Method> getters = typeDef.getAllGetters();

        getters.entrySet().forEach(e -> unresolved.put(e.getKey(), () -> invoke(e.getValue(), view)));
    }

    public Object getView() {
        return view;
    }

    private static Object invoke(Method method, Object view) {
        try {
            method.setAccessible(true);
            return method.invoke(view);

        } catch (IllegalAccessException | InvocationTargetException e) {
            LOGGER.warn("failed to invoke method: " + method);
            return null;
        }
    }

    @Override
    public int size() {
        return unresolved.size();
    }

    @Override
    public boolean isEmpty() {
        return unresolved.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return unresolved.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        resolver.ensure();
        return resolved.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        if (key instanceof String) {

            if (resolved.containsKey(key)) {
                return resolved.get(key);

            } else {
                Supplier<Object> supplier = unresolved.get(key);

                if (supplier != null) {
                    Object value = convertValue(supplier.get());
                    resolved.put((String) key, value);
                    return value;

                } else {
                    return null;
                }
            }
        } else {
            return null;
        }
    }

    @Override
    public Object put(String key, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> keySet() {
        return unresolved.keySet();
    }

    @Override
    public Collection<Object> values() {
        resolver.ensure();
        return resolved.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        resolver.ensure();
        return resolved.entrySet();
    }

    @Override
    public String toString() {

        return "{" + StringUtils.join(entrySet()
                .stream()
                .map((e) -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.toList()), ", ") + "}";
    }

    /*
    public static final String REFERENTIAL_TEXT_TYPE = "referentialText";
    public static final String REGION_TYPE = "region";
     */
    private Object convertValue(Object value) {

        if (value instanceof CharSequence) {
            return value;

        } else if (value instanceof Boolean) {
            return value;

        } else if (value instanceof Number) {
            return value;

        } else if (value instanceof Date) {
            return ((Date) value).getTime();

        } else if (value instanceof URI || value instanceof URL) {
            return value.toString();

        } else if (value instanceof Locale) {
            return ((Locale) value).toLanguageTag();

        } else if (value instanceof File) {
            return ((File) value).getName();

        } else if (value instanceof UUID) {
            return value.toString();

        } else if (value instanceof Location) {
            return ImmutableMap.of(
                    "x", ((Location) value).getX(),
                    "y", ((Location) value).getY());

        } else if (value instanceof Metric) {
            return ((Metric) value).getSum();

        } else if (value instanceof State) { // should we handle this?
            return ((State) value).getSimpleValues();

        } else if (value instanceof Recordable) { // should we handle this?
            return ((Recordable) value).getState().getSimpleValues();

        } else if (value instanceof Iterable) {
            List<Object> immutableViewList = new ArrayList<>();

            for (Object item : (Iterable<?>) value) {
                immutableViewList.add(convertValue(item));
            }
            value = immutableViewList;

        } else if (value instanceof ViewMap) { // pass through
            return value;

        } else if (value instanceof Map) {
            // TODO: should probably wrap the map
            return value;

        } else if (value != null) {
            value = new ViewMap(value);
        }

        return value;
    }
}
