package com.psddev.cms.db;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

    @Override
    public void renderObject(
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse,
            HtmlWriter writer)
            throws IOException {
        writer.writeRaw(getText());
    }
}
