package com.psddev.cms.db;

import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.psddev.dari.db.State;
import com.psddev.dari.util.HtmlGrid;
import com.psddev.dari.util.JspUtils;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.PageContextFilter;
import com.psddev.dari.util.Settings;
import com.psddev.dari.util.StorageItem;
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
        return object != null ? State.getInstance(object).as(Renderer.Data.class).getListLayouts().get(field) : null;
    }

    /**
     * Returns the plain {@linkplain StorageItem CDN} URL associated
     * with the given {@code servletPath}.
     */
    public static String plainResource(String servletPath) {
        ServletContext servletContext = null;
        HttpServletRequest request = null;

        try {
            servletContext = PageContextFilter.Static.getServletContext();
            request = PageContextFilter.Static.getRequest();
        } catch (IllegalStateException ex) {
        }

        if (servletContext != null && request != null) {
            HtmlGrid.Static.addStyleSheet(request, servletPath);

            if (ObjectUtils.coalesce(
                    Settings.get(Boolean.class, "cms/isResourceInStorage"),
                    Settings.isProduction())) {
                StorageItem item = StorageItem.Static.getPlainResource(null, servletContext, servletPath);

                if (item != null) {
                    return JspUtils.isSecure(request) ?
                            item.getSecurePublicUrl() :
                            item.getPublicUrl();
                }
            }
        }

        return servletPath;
    }

    /**
     * Returns the plain or gzipped {@linkplain StorageItem CDN} URL
     * associated with the given {@code servletPath}.
     */
    public static String resource(String servletPath) {
        ServletContext servletContext = null;
        HttpServletRequest request = null;

        try {
            servletContext = PageContextFilter.Static.getServletContext();
            request = PageContextFilter.Static.getRequest();
        } catch (IllegalStateException ex) {
        }

        if (servletContext != null && request != null) {
            HtmlGrid.Static.addStyleSheet(request, servletPath);

            if (ObjectUtils.coalesce(
                    Settings.get(Boolean.class, "cms/isResourceInStorage"),
                    Settings.isProduction())) {

                String encodings = request.getHeader("Accept-Encoding");
                StorageItem item = ObjectUtils.isBlank(encodings) || !encodings.contains("gzip") ?
                        StorageItem.Static.getPlainResource(null, servletContext, servletPath) :
                        StorageItem.Static.getGzippedResource(null, servletContext, servletPath);

                if (item != null) {
                    return JspUtils.isSecure(request) ?
                            item.getSecurePublicUrl() :
                            item.getPublicUrl();
                }
            }
        }

        return servletPath;
    }
}
