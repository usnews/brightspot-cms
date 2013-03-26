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

    @Override
    protected void doRequest(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        if (!ObjectUtils.to(boolean.class, request.getParameter("_fields"))) {
            chain.doFilter(request, response);

        } else {
            if (PageFilter.Static.getMainObject(request) == null) {
                chain.doFilter(request, response);

            } else {
                try {
                    JspBufferFilter.Static.overrideBuffer(0);

                    FieldAccessResponse fieldAccessResponse = new FieldAccessResponse(response);

                    try {
                        FieldAccessListener listener = new FieldAccessListener(fieldAccessResponse);

                        try {
                            State.Static.addListener(listener);
                            chain.doFilter(request, fieldAccessResponse);
                        } finally {
                            State.Static.removeListener(listener);
                        }

                    } finally {
                        fieldAccessResponse.writePending();
                    }

                } finally {
                    JspBufferFilter.Static.restoreBuffer();
                }
            }
        }
    }

    private static class FieldAccessListener extends State.Listener {

        private final FieldAccessResponse response;

        public FieldAccessListener(FieldAccessResponse response) {
            this.response = response;
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

            try {
                response.writeLazily(marker.toString());
            } catch (IOException error) {
            }
        }
    }

    private static class FieldAccessResponse extends HttpServletResponseWrapper {

        private LazyWriter fieldAccessWriter;
        private PrintWriter writer;

        public FieldAccessResponse(HttpServletResponse response) {
            super(response);
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            if (writer == null) {
                fieldAccessWriter = new LazyWriter(super.getWriter());
                writer = new PrintWriter(fieldAccessWriter);
            }

            return writer;
        }

        public void writeLazily(String string) throws IOException {
            getWriter();
            fieldAccessWriter.writeLazily(string);
        }

        public void writePending() throws IOException {
            getWriter();
            fieldAccessWriter.writePending();
        }
    }
}
