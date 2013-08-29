package com.psddev.cms.db;

import com.psddev.dari.db.Modification;
import com.psddev.dari.db.Recordable.BeanProperty;
import com.psddev.dari.db.Reference;

@ToolUi.Hidden
@BeanProperty("rte")
public class RichTextReference extends Modification<Reference> {

    @ToolUi.Hidden
    private String alignment;
    @ToolUi.Hidden
    private String preview;
    @ToolUi.Hidden
    private String label;

    public String getAlignment() {
        return alignment;
    }
    public void setAlignment(String alignment) {
        this.alignment = alignment;
    }
    public String getPreview() {
        return preview;
    }
    public void setPreview(String preview) {
        this.preview = preview;
    }
    @Override
    public String getLabel() {
        return label;
    }
    public void setLabel(String label) {
        this.label = label;
    }
}
