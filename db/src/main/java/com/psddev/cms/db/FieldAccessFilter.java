package com.psddev.cms.db;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import com.psddev.dari.db.State;
import com.psddev.dari.util.AbstractFilter;
import com.psddev.dari.util.JspBufferFilter;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.StringUtils;

/**
 * Internal filter that adds {@code <span data-field>} to the response
 * whenever an object field is accessed.
 */
public class FieldAccessFilter extends AbstractFilter {

    private static final String ATTRIBUTE_PREFIX = FieldAccessFilter.class.getName() + ".";
    private static final String CURRENT_RESPONSE_ATTRIBUTE = ATTRIBUTE_PREFIX + "currentResponse";

    @Override
    protected void doDispatch(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain)
            throws Exception {

        if (ObjectUtils.to(boolean.class, request.getParameter("_fields")) &&
                PageFilter.Static.getMainObject(request) != null) {
            super.doDispatch(request, response, chain);

        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    protected void doInclude(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        Object current = request.getAttribute(CURRENT_RESPONSE_ATTRIBUTE);
        FieldAccessResponse fieldAccessResponse = new FieldAccessResponse(request, response);

        try {
            request.setAttribute(CURRENT_RESPONSE_ATTRIBUTE, fieldAccessResponse);
            chain.doFilter(request, fieldAccessResponse);

        } finally {
            request.setAttribute(CURRENT_RESPONSE_ATTRIBUTE, current);
            fieldAccessResponse.getLazyWriter().writePending();
        }
    }

    @Override
    protected void doRequest(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        try {
            JspBufferFilter.Static.overrideBuffer(0);

            FieldAccessListener listener = new FieldAccessListener(request);

            try {
                State.Static.addListener(listener);
                doInclude(request, response, chain);

            } finally {
                State.Static.removeListener(listener);
            }

        } finally {
            JspBufferFilter.Static.restoreBuffer();
        }
    }

    private static class FieldAccessListener extends State.Listener {

        private final HttpServletRequest request;

        public FieldAccessListener(HttpServletRequest request) {
            this.request = request;
        }

        @Override
        public void beforeFieldGet(State state, String name) {
            StringBuilder marker = new StringBuilder();

            marker.append("<span style=\"display: none;\" data-name=\"");
            marker.append(StringUtils.escapeHtml(state.getId().toString()));
            marker.append("/");
            marker.append(StringUtils.escapeHtml(name));
            marker.append("\">");
            marker.append("</span>");

            FieldAccessResponse response = (FieldAccessResponse) request.getAttribute(CURRENT_RESPONSE_ATTRIBUTE);

            if (response != null) {
                try {
                    response.getLazyWriter().writeLazily(marker.toString());
                } catch (IOException error) {
                }
            }
        }
    }

    private static class FieldAccessResponse extends HttpServletResponseWrapper {

        private final HttpServletRequest request;
        private LazyWriter lazyWriter;
        private PrintWriter writer;

        public FieldAccessResponse(HttpServletRequest request, HttpServletResponse response) {
            super(response);
            this.request = request;
        }

        public LazyWriter getLazyWriter() throws IOException {
            if (lazyWriter == null) {
                lazyWriter = new LazyWriter(request, super.getWriter());
            }
            return lazyWriter;
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            if (writer == null) {
                writer = new PrintWriter(getLazyWriter());
            }
            return writer;
        }
    }
}
