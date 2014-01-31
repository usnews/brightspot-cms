package com.psddev.cms.db;

import com.psddev.dari.db.Modification;
import com.psddev.dari.db.Reference;

@RichTextReference.BeanProperty("rte")
@ToolUi.Hidden
public class RichTextReference extends Modification<Reference> {

    @ToolUi.Hidden
    private String alignment;

    @ToolUi.Hidden
    private String preview;

    @ToolUi.Hidden
    private String label;

    @ToolUi.Hidden
    private String imageSize;

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

    public String getImageSize() {
        return imageSize;
    }

    public void setImageSize(String imageSize) {
        this.imageSize = imageSize;
    }

    @Override
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
