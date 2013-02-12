package com.psddev.cms.db;

import java.util.List;

import com.psddev.dari.db.State;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.StringUtils;

public final class ElFunctionUtils {

    private ElFunctionUtils() {
    }

    /**
     * Escapes the given {@code string} so that it's safe to use in HTML.
     *
     * @param string If {@code null}, returns an empty string.
     * @return Never {@code null}.
     */
    public static String html(String string) {
        return string != null ? StringUtils.escapeHtml(string) : "";
    }

    /**
     * Returns {@code true} if the given {@code object} is an instance of the
     * class represented by the given {@code className}.
     *
     * @throws IllegalArgumentException If there isn't a class with the given
     * {@code className}.
     */
    public static boolean instanceOf(Object object, String className) {
        Class<?> c = ObjectUtils.getClassByName(className);

        if (c != null) {
            return c.isInstance(object);

        } else {
            throw new IllegalArgumentException(String.format(
                    "[%s] is not a valid class name!", className));
        }
    }

    /**
     * Escapes the given {@code string} so that it's safe to use in
     * JavaScript.
     *
     * @param string If {@code null}, returns an empty string.
     * @return Never {@code null}.
     */
    public static String js(String string) {
        return string != null ? StringUtils.escapeJavaScript(string) : "";
    }

    public static List<String> listLayouts(Object object, String field) {
        return State.getInstance(object).as(Renderable.Data.class).getListLayouts().get(field);
    }
}
