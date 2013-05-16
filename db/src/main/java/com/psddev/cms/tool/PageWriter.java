package com.psddev.cms.tool;

import com.psddev.dari.util.HtmlWriter;

import java.io.IOException;
import java.io.Writer;

/** @deprecated Use {@link ToolPageContext} directly instead. */
@Deprecated
public class PageWriter extends HtmlWriter {

    public PageWriter(Writer writer) {
        super(writer);
    }

    /**
     * Writes a label, or the given {@code defaultLabel} if one can't be
     * found, of the given {@code object}.
     */
    public void objectLabelOrDefault(Object object, String defaultLabel) throws IOException {
        html(ToolPageContext.Static.getObjectLabelOrDefault(object, defaultLabel));
    }

    /** Writes a label of the given {@code object}. */
    public void objectLabel(Object object) throws IOException {
        html(ToolPageContext.Static.getObjectLabel(object));
    }

    /**
     * Writes a label, or the given {@code defaultLabel} if one can't be
     * found, of the type of the given {@code object}.
     */
    public void typeLabelOrDefault(Object object, String defaultLabel) throws IOException {
        html(ToolPageContext.Static.getTypeLabelOrDefault(object, defaultLabel));
    }

    /** Writes a label of the type of the given {@code object}. */
    public void typeLabel(Object object) throws IOException {
        html(ToolPageContext.Static.getTypeLabel(object));
    }
}
