package com.psddev.cms.tool;

import com.psddev.dari.db.ObjectType;
import com.psddev.dari.db.State;
import com.psddev.dari.util.HtmlWriter;
import com.psddev.dari.util.ObjectUtils;

import java.io.IOException;
import java.io.Writer;
import java.util.UUID;

public class ToolPageWriter extends HtmlWriter {

    public ToolPageWriter(Writer writer) {
        super(writer);
    }

    /**
     * Writes a label, or the given {@code defaultLabel} if one can't be
     * found, of the given {@code object}.
     */
    public void objectLabelOrDefault(Object object, String defaultLabel) throws IOException {
        State state = State.getInstance(object);
        String label = defaultLabel;

        if (state != null) {
            label = state.getLabel();

            if (ObjectUtils.to(UUID.class, label) != null) {
                label = defaultLabel;
            }
        }

        start("span", "class", "objectLabel");
            html(label);
        end();
    }

    /** Writes a label of the given {@code object}. */
    public void objectLabel(Object object) throws IOException {
        State state = State.getInstance(object);

        start("span", "class", "objectLabel");
            html(state != null ? state.getLabel() : "Not Available");
        end();
    }

    /**
     * Writes a label, or the given {@code defaultLabel} if one can't be
     * found, of the type of the given {@code object}.
     */
    public void typeLabelOrDefault(Object object, String defaultLabel) throws IOException {
        State state = State.getInstance(object);
        String label = defaultLabel;

        if (state != null) {
            ObjectType type = state.getType();

            if (type != null) {
                label = type.getLabel();
            }
        }

        start("span", "class", "typeLabel");
            html(label);
        end();
    }

    /** Writes a label of the type of the given {@code object}. */
    public void typeLabel(Object object) throws IOException {
        typeLabelOrDefault(object, "Unknown Type");
    }
}
