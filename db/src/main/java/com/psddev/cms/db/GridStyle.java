package com.psddev.cms.db;

import com.psddev.dari.db.ObjectType;
import com.psddev.dari.db.Record;

@GridStyle.Embedded
public class GridStyle extends Record {

    private ObjectType type;
    private ContentStyle style;

    public ObjectType getType() {
        return type;
    }

    public void setType(ObjectType type) {
        this.type = type;
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
        ContentStyle style = getStyle();

        if (type != null && style != null) {
            label.append(type.getLabel());
            label.append(" Type \u2192 ");
            label.append(style.getLabel());
            label.append(" Style");
        }

        return label.toString();
    }
}
