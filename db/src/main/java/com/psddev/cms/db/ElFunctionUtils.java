package com.psddev.cms.db;

import java.util.List;

import com.psddev.dari.db.State;
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
