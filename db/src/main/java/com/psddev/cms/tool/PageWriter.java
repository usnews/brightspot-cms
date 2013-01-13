package com.psddev.cms.tool;

import com.psddev.dari.util.HtmlWriter;

import java.io.IOException;
import java.io.Writer;

public class PageWriter extends HtmlWriter {

    public PageWriter(Writer writer) {
        super(writer);
    }

    /**
     * Writes a label, or the given {@code defaultLabel} if one can't be
     * found, of the given {@code object}.
     */
    public void objectLabelOrDefault(Object object, String defaultLabel) throws IOException {
        start("span", "class", "objectLabel");
            html(ToolPageContext.Static.getObjectLabelOrDefault(object, defaultLabel));
        end();
    }

    /** Writes a label of the given {@code object}. */
    public void objectLabel(Object object) throws IOException {
        start("span", "class", "objectLabel");
            html(ToolPageContext.Static.getObjectLabel(object));
        end();
    }

    /**
     * Writes a label, or the given {@code defaultLabel} if one can't be
     * found, of the type of the given {@code object}.
     */
    public void typeLabelOrDefault(Object object, String defaultLabel) throws IOException {
        start("span", "class", "typeLabel");
            html(ToolPageContext.Static.getTypeLabelOrDefault(object, defaultLabel));
        end();
    }

    /** Writes a label of the type of the given {@code object}. */
    public void typeLabel(Object object) throws IOException {
        start("span", "class", "typeLabel");
            html(ToolPageContext.Static.getTypeLabel(object));
        end();
    }
}
