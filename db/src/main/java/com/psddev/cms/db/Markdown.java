package com.psddev.cms.db;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.rjeschke.txtmark.Processor;
import com.psddev.dari.db.Record;
import com.psddev.dari.util.HtmlWriter;
import com.psddev.dari.util.ObjectUtils;

/**
 * @see <a href="http://daringfireball.net/projects/markdown/">Markdown</a>
 */
@Record.Embedded
public class Markdown extends Record implements Renderer {

    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public void renderObject(
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse,
            HtmlWriter writer)
            throws IOException {
        String text = getText();

        if (!ObjectUtils.isBlank(text)) {
            writer.writeRaw(Processor.process(text));
        }
    }
}
