package com.psddev.cms.db;

import com.psddev.dari.db.State;
import com.psddev.dari.util.AbstractFilter;
import com.psddev.dari.util.JspBufferFilter;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.StringUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Locale;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

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
            Object mainObject = PageFilter.Static.getMainObject(request);

            if (mainObject == null) {
                chain.doFilter(request, response);

            } else {
                try {
                    JspBufferFilter.Static.setBufferOverride(0);

                    FieldAccessResponse fieldAccessResponse = new FieldAccessResponse(response);

                    try {
                        FieldAccessListener listener = new FieldAccessListener(mainObject, fieldAccessResponse);

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
                    JspBufferFilter.Static.removeBufferOverride();
                }
            }
        }
    }

    private static class FieldAccessListener extends State.Listener {

        private final Object mainObject;
        private final FieldAccessResponse response;

        public FieldAccessListener(Object mainObject, FieldAccessResponse response) {
            this.mainObject = mainObject;
            this.response = response;
        }

        @Override
        public void beforeFieldGet(State state, String name) {
            if (mainObject.equals(state.getOriginalObject())) {
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
    }

    private static class FieldAccessResponse extends HttpServletResponseWrapper {

        private FieldAccessWriter fieldAccessWriter;
        private PrintWriter writer;

        public FieldAccessResponse(HttpServletResponse response) {
            super(response);
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            if (writer == null) {
                fieldAccessWriter = new FieldAccessWriter(super.getWriter());
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

    private static class FieldAccessWriter extends Writer {

        private final Writer delegate;
        private final StringBuilder lazy = new StringBuilder();
        private final StringBuilder pending = new StringBuilder();

        private boolean inString;
        private boolean stringEscaping;
        private char stringStartLetter;

        private boolean commentStarting;
        private boolean inSingleComment;
        private boolean inMultiComment;
        private boolean multiCommentEnding;

        private boolean inTag;
        private final StringBuilder tagName = new StringBuilder();
        private boolean tagNameFound;
        private boolean inScriptOrStyle;
        private boolean inBody;

        public FieldAccessWriter(Writer delegate) {
            this.delegate = delegate;
        }

        @Override
        public void write(char[] buffer, int offset, int length) throws IOException {
            for (length += offset; offset < length; ++ offset) {
                char letter = buffer[offset];

                pending.append(letter);

                if (inString) {
                    if (stringEscaping) {
                        stringEscaping = false;

                    } else if (letter == '\\') {
                        stringEscaping = true;

                    } else if (letter == stringStartLetter) {
                        inString = false;
                    }

                } else if (commentStarting) {
                    commentStarting = false;

                    if (letter == '/') {
                        inSingleComment = true;

                    } else if (letter == '*') {
                        inMultiComment = true;
                    }

                } else if (inSingleComment) {
                    if (letter == '\r' || letter == '\n') {
                        inSingleComment = false;
                    }

                } else if (inMultiComment) {
                    if (letter == '*') {
                        multiCommentEnding = true;

                    } else if (multiCommentEnding && letter == '/') {
                        inMultiComment = false;
                        multiCommentEnding = false;

                    } else {
                        multiCommentEnding = false;
                    }

                } else if (letter == '<') {
                    inTag = true;
                    tagName.setLength(0);
                    tagNameFound = false;
                    inScriptOrStyle = false;

                } else if (inTag) {
                    boolean endTag = letter == '>';

                    if (endTag || Character.isWhitespace(letter)) {
                        tagNameFound = true;

                    } else if (!tagNameFound) {
                        tagName.append(letter);
                    }

                    if (endTag) {
                        String tagNameLc = tagName.toString().toLowerCase(Locale.ENGLISH);

                        if ("script".equals(tagNameLc) || "style".equals(tagNameLc)) {
                            inScriptOrStyle = true;

                        } else if ("/script".equals(tagNameLc) || "/style".equals(tagNameLc)) {
                            inScriptOrStyle = false;

                        } else if ("body".equals(tagNameLc)) {
                            inBody = true;
                        }

                        writePending();

                        if (inBody && lazy.length() > 0) {
                            delegate.append(lazy);
                            lazy.setLength(0);
                        }

                        inTag = false;

                    } else if (letter == '\'' || letter == '"') {
                        inString = true;
                        stringStartLetter = letter;
                    }

                } else if (inScriptOrStyle) {
                    if (letter == '\'' || letter == '"') {
                        inString = true;
                        stringStartLetter = letter;

                    } else if (letter == '/') {
                        commentStarting = true;
                    }
                }
            }
        }

        public void writeLazily(String string) throws IOException {
            if (!inBody || inTag || inScriptOrStyle) {
                lazy.append(string);

            } else {
                delegate.write(string);
            }
        }

        public void writePending() throws IOException {
            if (pending.length() == 0) {
                return;
            }

            delegate.append(pending);
            pending.setLength(0);
        }

        @Override
        public void flush() throws IOException {
            delegate.flush();
        }

        @Override
        public void close() throws IOException {
            writePending();
            delegate.close();
        }
    }
}
