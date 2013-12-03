package com.psddev.cms.db;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.psddev.dari.db.Record;
import com.psddev.dari.util.HtmlWriter;

@Text.DisplayName("Raw HTML")
@ToolUi.Referenceable
public class Text extends Content implements Renderer {

    @Indexed(unique = true)
    @Required
    private String name;

    @DisplayName("HTML")
    @ToolUi.CodeType("text/html")
    private String text;

    @ToolUi.Tab("Advanced")
    private Integer statusCode;

    @ToolUi.Tab("Advanced")
    private List<Header> headers;

    /** Returns the name. */
    public String getName() {
        return name;
    }

    /** Sets the name. */
    public void setName(String name) {
        this.name = name;
    }

    /** Returns the text. */
    public String getText() {
        return text;
    }

    /** Sets the text. */
    public void setText(String text) {
        this.text = text;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public List<Header> getHeaders() {
        if (headers == null) {
            headers = new ArrayList<Header>();
        }
        return headers;
    }

    public void setHeaders(List<Header> headers) {
        this.headers = headers;
    }

    @Override
    public void renderObject(
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse,
            HtmlWriter writer)
            throws IOException {

        Integer statusCode = getStatusCode();

        if (statusCode != null) {
            httpResponse.setStatus(statusCode);
        }

        for (Header header : headers) {
            httpResponse.addHeader(header.getName(), header.getValue());
        }

        writer.writeRaw(getText());
    }

    @Embedded
    public static class Header extends Record {

        @Required
        private String name;

        @Required
        private String value;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
