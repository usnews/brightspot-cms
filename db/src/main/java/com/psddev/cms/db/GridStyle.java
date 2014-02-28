package com.psddev.cms.db;

import com.psddev.dari.db.ObjectType;
import com.psddev.dari.db.Record;
import com.psddev.dari.util.ObjectUtils;

@GridStyle.Embedded
public class GridStyle extends Record {

    @Required
    private ObjectType type;

    private String context;

    @Required
    private ContentStyle style;

    public ObjectType getType() {
        return type;
    }

    public void setType(ObjectType type) {
        this.type = type;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public ContentStyle getStyle() {
        return style;
    }

    public void setStyle(ContentStyle style) {
        this.style = style;
    }

    @Override
    public String getLabel() {
        StringBuilder label = new StringBuilder();
        ObjectType type = getType();
        String context = getContext();
        ContentStyle style = getStyle();

        if (type != null && style != null) {
            label.append("Type: ");
            label.append(type.getLabel());

            if (!ObjectUtils.isBlank(context)) {
                label.append(", Context: ");
                label.append(context);
            }

            label.append(" \u2192 Style: ");
            label.append(style.getLabel());
        }

        return label.toString();
    }
}
