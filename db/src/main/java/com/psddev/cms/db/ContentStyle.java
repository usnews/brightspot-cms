package com.psddev.cms.db;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.psddev.cms.db.style.AbstractBlock;
import com.psddev.dari.db.Record;
import com.psddev.dari.db.Recordable;
import com.psddev.dari.util.HtmlWriter;

public class ContentStyle extends AbstractBlock implements Renderer {

    @ToolUi.Tab("Preview")
    @Types({ Record.class })
    private Recordable previewContent;

    @ToolUi.Tab("Preview")
    private Integer previewWidth;

    @ToolUi.Tab("Preview")
    private Integer previewHeight;

    public Object getPreviewContent() {
        return previewContent;
    }

    public void setPreviewContent(Object previewContent) {
        this.previewContent = (Recordable) previewContent;
    }

    public Integer getPreviewWidth() {
        return previewWidth;
    }

    public void setPreviewWidth(Integer previewWidth) {
        this.previewWidth = previewWidth;
    }

    public Integer getPreviewHeight() {
        return previewHeight;
    }

    public void setPreviewHeight(Integer previewHeight) {
        this.previewHeight = previewHeight;
    }

    public void writeCss(HtmlWriter writer) throws IOException {
        writeCss(writer, ".bsp-block-" + getInternalName());
    }

    @Override
    public void renderObject(
            HttpServletRequest request,
            HttpServletResponse response,
            HtmlWriter writer)
            throws IOException, ServletException {

        Object previewContent = getPreviewContent();

        if (previewContent == null) {
            writer.writeHtml("Please pick a preview content.");

        } else {
            Integer previewHeight = getPreviewHeight();
            Integer previewWidth = getPreviewWidth();

            writer.writeStart("style", "type", "text/css");
                writeCss(writer);
            writer.writeEnd();

            writer.writeStart("div", "style", writer.cssString(
                    "height", previewHeight != null ? previewHeight + "px" : "auto",
                    "width", previewWidth != null ? previewWidth + "px" : "auto"));
                writeHtml(writer, previewContent);
            writer.writeEnd();
        }
    }
}
