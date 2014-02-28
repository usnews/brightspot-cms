package com.psddev.cms.db;

import com.psddev.dari.db.Record;
import com.psddev.dari.util.ObjectUtils;

@GridContext.Embedded
public class GridContext extends Record {

    private int area;
    private String context;

    public int getArea() {
        return area;
    }

    public void setArea(int area) {
        this.area = area;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    @Override
    public String getLabel() {
        StringBuilder label = new StringBuilder();
        int area = getArea();
        String context = getContext();

        label.append("Area: ");
        label.append(area);

        if (!ObjectUtils.isBlank(context)) {
            label.append(" \u2192 Context: ");
            label.append(context);
        }

        return label.toString();
    }
}
