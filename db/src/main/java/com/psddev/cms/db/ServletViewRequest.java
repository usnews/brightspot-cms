package com.psddev.cms.db;

import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
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

    private static final String HTTP_REQUEST_NAMESPACE = "http.request";
    private static final String HTTP_PARAMETER_NAMESPACE = "http.parameter";
    private static final String HTTP_HEADER_NAMESPACE = "http.header";
    private static final String SERVLET_ATTRIBUTE_NAMESPACE = "servlet.attribute";
    private static final String CMS_OBJECTS_NAMESPACE = "cms.objects";

    private HttpServletRequest request;
    private Map<String, Namespace> namespaces = new HashMap<>();

    public ServletViewRequest(HttpServletRequest request) {
        this.request = request;
        this.namespaces.put(HTTP_REQUEST_NAMESPACE, new HttpRequestNamespace());
        this.namespaces.put(HTTP_PARAMETER_NAMESPACE, new HttpParameterNamespace());
        this.namespaces.put(HTTP_HEADER_NAMESPACE, new HttpHeaderNamespace());
        this.namespaces.put(SERVLET_ATTRIBUTE_NAMESPACE, new ServletAttributeNamespace());
        this.namespaces.put(CMS_OBJECTS_NAMESPACE, new CmsObjectsNamespace());
    }

    @Override
    public Set<String> getParameterNamespaces() {
        return namespaces.keySet();
    }

    @Override
    public Set<String> getParameterNames(String namespace) {
        Namespace ns = namespaces.get(namespace);
        return ns != null ? ns.getParameterNames(request) : Collections.emptySet();
    }

    // To support backward compatibility from 3.1 when the concept of namespace
    // had not yet been introduced.
    @Deprecated
    public <T> Stream<T> getParameter(Class<T> returnType, String name) {
        return getParameter(returnType, HTTP_PARAMETER_NAMESPACE, name);
    }

    @Override
    protected Object getParameterValue(String namespace, String name) {
        Namespace ns = namespaces.get(namespace);
        return ns != null ? ns.getParameter(request, name) : null;
    }

    /**
     * Gets the main object associated with the current request.
     *
     * @param request the current view request.
     * @return the main object.
     */
    public static Object getMainObject(ViewRequest request) {
        return request.getParameter(Object.class, CMS_OBJECTS_NAMESPACE, CmsObjectsNamespace.MAIN_OBJECT_KEY).findFirst().orElse(null);
    }

    /**
     * Gets the path used to find the {@link #getMainObject(ViewRequest) main object}.
     *
     * @param request the current view request.
     * @return the path used to find the main object.
     */
    public static String getMainObjectPath(ViewRequest request) {
        return request.getParameter(String.class, CMS_OBJECTS_NAMESPACE, CmsObjectsNamespace.PATH_KEY).findFirst().orElse(null);
    }

    /**
     * Gets the current site.
     *
     * @param request the current view request.
     * @return the current site.
     */
    public static Site getSite(ViewRequest request) {
        return request.getParameter(Site.class, CMS_OBJECTS_NAMESPACE, CmsObjectsNamespace.SITE_KEY).findFirst().orElse(null);
    }

    /**
     * Get the current HTTP request URL.
     *
     * @param request the current view request.
     * @return the HTTP request URL for the current view request.
     */
    public static String getHttpRequestUrl(ViewRequest request) {
        return request.getParameter(String.class, HTTP_REQUEST_NAMESPACE, HttpRequestNamespace.URL_KEY).findFirst().orElse(null);
    }

    /**
     * Get the current HTTP request URI.
     *
     * @param request the current view request.
     * @return the HTTP request URI for the current view request.
     */
    public static String getHttpRequestUri(ViewRequest request) {
        return request.getParameter(String.class, HTTP_REQUEST_NAMESPACE, HttpRequestNamespace.URI_KEY).findFirst().orElse(null);
    }

    /**
     * Get the current HTTP request method.
     *
     * @param request the current view request.
     * @return the HTTP request method for the current view request.
     */
    public static String getHttpRequestMethod(ViewRequest request) {
        return request.getParameter(String.class, HTTP_REQUEST_NAMESPACE, HttpRequestNamespace.METHOD_KEY).findFirst().orElse(null);
    }

    /**
     * Get the current HTTP request remote address.
     *
     * @param request the current view request.
     * @return the remote IP address for the current view request.
     */
    public static String getHttpRequestRemoteAddress(ViewRequest request) {
        return request.getParameter(String.class, HTTP_REQUEST_NAMESPACE, HttpRequestNamespace.REMOTE_ADDRESS_KEY).findFirst().orElse(null);
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
        return request.getParameter(returnType, HTTP_PARAMETER_NAMESPACE, name);
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
        return request.getParameter(returnType, HTTP_HEADER_NAMESPACE, name);
    }

    /**
     * Get an HTTP Servlet Request attribute.
     *
     * @param returnType the return type.
     * @param name the HTTP Servlet request attribute name.
     * @param request the current view request.
     * @param <T> the return type.
     * @return the HTTP Servlet request attribute value.
     */
    public static <T> T getServletRequestAttribute(Class<T> returnType, String name, ViewRequest request) {
        return request.getParameter(returnType, SERVLET_ATTRIBUTE_NAMESPACE, name).findFirst().orElse(null);
    }

    private static interface Namespace {

        Set<String> getParameterNames(HttpServletRequest request);

        Object getParameter(HttpServletRequest request, String name);
    }

    private static class HttpParameterNamespace implements Namespace {

        @Override
        public Set<String> getParameterNames(HttpServletRequest request) {
            return new LinkedHashSet<>(Collections.list(request.getParameterNames()));
        }

        @Override
        public Object getParameter(HttpServletRequest request, String name) {
            String[] parameters = request.getParameterValues(name);
            if (parameters != null) {
                if (parameters.length == 1) {
                    return parameters[0];
                } else {
                    return Arrays.asList(parameters);
                }
            } else {
                return null;
            }
        }
    }

    private static final class HttpHeaderNamespace implements Namespace {

        @Override
        public Set<String> getParameterNames(HttpServletRequest request) {
            return new LinkedHashSet<>(Collections.list(request.getHeaderNames()));
        }

        @Override
        public Object getParameter(HttpServletRequest request, String name) {
            Enumeration<String> headers = request.getHeaders(name);
            if (headers != null) {
                return Collections.list(headers);
            } else {
                return null;
            }
        }
    }

    private static final class ServletAttributeNamespace implements Namespace {

        @Override
        public Set<String> getParameterNames(HttpServletRequest request) {
            return new LinkedHashSet<>(Collections.list(request.getAttributeNames()));
        }

        @Override
        public Object getParameter(HttpServletRequest request, String name) {
            return request.getAttribute(name);
        }
    }

    private static final class HttpRequestNamespace implements Namespace {

        private static final String URI_KEY = "uri";
        private static final String URL_KEY = "url";
        private static final String METHOD_KEY = "method";
        private static final String REMOTE_ADDRESS_KEY = "remoteAddress";

        private Map<String, Function<HttpServletRequest, String>> parameters = new HashMap<>();

        private HttpRequestNamespace() {
            parameters.put(URI_KEY, HttpServletRequest::getRequestURI);
            parameters.put(URL_KEY, request -> request.getRequestURL().toString());
            parameters.put(METHOD_KEY, HttpServletRequest::getMethod);
            parameters.put(REMOTE_ADDRESS_KEY, JspUtils::getRemoteAddress);
        }

        @Override
        public Set<String> getParameterNames(HttpServletRequest request) {
            return new LinkedHashSet<>(Arrays.asList(URI_KEY, URL_KEY, METHOD_KEY, REMOTE_ADDRESS_KEY));
        }

        @Override
        public Object getParameter(HttpServletRequest request, String name) {
            Function<HttpServletRequest, String> function = parameters.get(name);
            return function != null ? function.apply(request) : null;
        }
    }

    private static final class CmsObjectsNamespace implements Namespace {

        private static final String MAIN_OBJECT_KEY = "mainObject";
        private static final String PAGE_KEY = "page";
        private static final String PATH_KEY = "path";
        private static final String PROFILE_KEY = "profile";
        private static final String SITE_KEY = "site";

        private Map<String, Function<HttpServletRequest, Object>> parameters = new HashMap<>();

        private CmsObjectsNamespace() {
            parameters.put(MAIN_OBJECT_KEY, PageFilter.Static::getMainObject);
            parameters.put(PAGE_KEY, PageFilter.Static::getPage);
            parameters.put(PATH_KEY, PageFilter.Static::getPath);
            parameters.put(PROFILE_KEY, PageFilter.Static::getProfile);
            parameters.put(SITE_KEY, PageFilter.Static::getSite);
        }

        @Override
        public Set<String> getParameterNames(HttpServletRequest request) {
            return new LinkedHashSet<>(Arrays.asList(MAIN_OBJECT_KEY, PAGE_KEY, PATH_KEY, PROFILE_KEY, SITE_KEY));
        }

        @Override
        public Object getParameter(HttpServletRequest request, String name) {
            Function<HttpServletRequest, Object> function = parameters.get(name);
            return function != null ? function.apply(request) : null;
        }
    }
}
