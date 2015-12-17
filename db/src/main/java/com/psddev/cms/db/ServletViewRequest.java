package com.psddev.cms.db;

import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.psddev.cms.view.AbstractViewRequest;
import com.psddev.cms.view.ViewRequest;
import com.psddev.dari.util.JspUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * ViewRequest implementation that uses the Java Servlet Spec for handling HTTP
 * requests.
 */
public class ServletViewRequest extends AbstractViewRequest {

    private HttpServletRequest request;

    private Set<String> parameterNames = null;

    public ServletViewRequest(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    protected Object getParameterValue(String name) {

        if (name != null) {

            if (name.startsWith(PageFilter.VIEW_REQUEST_HTTP_HEADER_PREFIX)) {
                Enumeration<String> headers = request.getHeaders(name.substring(PageFilter.VIEW_REQUEST_HTTP_HEADER_PREFIX.length()));

                if (headers != null) {
                    return Collections.list(headers);
                }

            } else if (name.startsWith(PageFilter.VIEW_REQUEST_HTTP_PARAMETER_PREFIX)) {
                String[] parameters = request.getParameterValues(name.substring(PageFilter.VIEW_REQUEST_HTTP_PARAMETER_PREFIX.length()));

                if (parameters != null) {
                    return Arrays.asList(parameters);
                }

            } else if (name.startsWith(PageFilter.VIEW_REQUEST_SERVLET_ATTRIBUTE_PREFIX)) {
                return request.getAttribute(name.substring(PageFilter.VIEW_REQUEST_SERVLET_ATTRIBUTE_PREFIX.length()));

            } else if (name.equals(PageFilter.VIEW_REQUEST_HTTP_REQUEST_URI)) {
                return request.getRequestURI();

            } else if (name.equals(PageFilter.VIEW_REQUEST_HTTP_REQUEST_URL)) {
                return request.getRequestURL().toString();

            } else if (name.equals(PageFilter.VIEW_REQUEST_HTTP_REQUEST_METHOD)) {
                return request.getMethod();

            } else if (name.equals(PageFilter.VIEW_REQUEST_HTTP_REQUEST_REMOTE_ADDRESS)) {
                return JspUtils.getRemoteAddress(request);

            } else if (name.equals(PageFilter.MAIN_OBJECT_ATTRIBUTE)) {
                return PageFilter.Static.getMainObject(request);

            } else if (name.equals(PageFilter.SITE_ATTRIBUTE)) {
                return PageFilter.Static.getSite(request);
            }
        }

        return null;
    }

    @Override
    public Set<String> getParameterNames() {

        if (parameterNames == null) {
            Set<String> set = new LinkedHashSet<>();
            set.addAll(Collections.list(request.getHeaderNames()).stream().map(
                    name -> PageFilter.VIEW_REQUEST_HTTP_HEADER_PREFIX + name).collect(Collectors.toList()));

            set.addAll(Collections.list(request.getParameterNames()).stream().map(
                    name -> PageFilter.VIEW_REQUEST_HTTP_PARAMETER_PREFIX + name).collect(Collectors.toList()));

            set.addAll(Collections.list(request.getAttributeNames()).stream().map(
                    name -> PageFilter.VIEW_REQUEST_SERVLET_ATTRIBUTE_PREFIX + name).collect(Collectors.toList()));

            set.add(PageFilter.VIEW_REQUEST_HTTP_REQUEST_URL);
            set.add(PageFilter.VIEW_REQUEST_HTTP_REQUEST_URI);
            set.add(PageFilter.VIEW_REQUEST_HTTP_REQUEST_METHOD);
            set.add(PageFilter.VIEW_REQUEST_HTTP_REQUEST_REMOTE_ADDRESS);
            set.add(PageFilter.MAIN_OBJECT_ATTRIBUTE);
            set.add(PageFilter.SITE_ATTRIBUTE);

            parameterNames = Collections.unmodifiableSet(set);
        }

        return parameterNames;
    }

    /**
     * Get the current HTTP request URL.
     *
     * @param request the current view request.
     * @return the HTTP request URL for the current view request.
     */
    public static String getHttpRequestUrl(ViewRequest request) {
        return request.getParameter(String.class, PageFilter.VIEW_REQUEST_HTTP_REQUEST_URL).findFirst().orElse(null);
    }

    /**
     * Get the current HTTP request URI.
     *
     * @param request the current view request.
     * @return the HTTP request URI for the current view request.
     */
    public static String getHttpRequestUri(ViewRequest request) {
        return request.getParameter(String.class, PageFilter.VIEW_REQUEST_HTTP_REQUEST_URI).findFirst().orElse(null);
    }

    /**
     * Get the current HTTP request method.
     *
     * @param request the current view request.
     * @return the HTTP request method for the current view request.
     */
    public static String getHttpRequestMethod(ViewRequest request) {
        return request.getParameter(String.class, PageFilter.VIEW_REQUEST_HTTP_REQUEST_METHOD).findFirst().orElse(null);
    }

    /**
     * Get the current HTTP request remote address.
     *
     * @param request the current view request.
     * @return the remote IP address for the current view request.
     */
    public static String getHttpRequestRemoteAddress(ViewRequest request) {
        return request.getParameter(String.class, PageFilter.VIEW_REQUEST_HTTP_REQUEST_REMOTE_ADDRESS).findFirst().orElse(null);
    }

    /**
     * Get an HTTP request parameter.
     *
     * @param returnType the stream type.
     * @param name the HTTP parameter name.
     * @param request the current view request.
     * @param <T> the stream type.
     * @return the HTTP request parameter value as a Stream.
     */
    public static <T> Stream<T> getHttpParameter(Class<T> returnType, String name, ViewRequest request) {
        return request.getParameter(returnType, PageFilter.VIEW_REQUEST_HTTP_PARAMETER_PREFIX + name);
    }

    /**
     * Get an HTTP request header.
     *
     * @param returnType the stream type.
     * @param name the HTTP header name.
     * @param request the current view request.
     * @param <T> the stream type.
     * @return the HTTP request header value as a Stream.
     */
    public static <T> Stream<T> getHttpHeader(Class<T> returnType, String name, ViewRequest request) {
        return request.getParameter(returnType, PageFilter.VIEW_REQUEST_HTTP_HEADER_PREFIX + name);
    }

    /**
     * Get an HTTP Servlet Request attribute.
     *
     * @param returnType the stream type.
     * @param name the HTTP Servlet request attribute name.
     * @param request the current view request.
     * @param <T> the stream type.
     * @return the HTTP Servlet request attribute value as a Stream.
     */
    public static <T> Stream<T> getServletRequestAttribute(Class<T> returnType, String name, ViewRequest request) {
        return request.getParameter(returnType, PageFilter.VIEW_REQUEST_SERVLET_ATTRIBUTE_PREFIX + name);
    }
}
