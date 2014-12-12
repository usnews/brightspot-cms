package com.psddev.cms.tool;

import java.util.ArrayList;
import java.util.List;

import com.psddev.dari.db.Record;

@DashboardColumn.Embedded
public class DashboardColumn extends Record {

    private int width;

    @Embedded
    private List<DashboardWidget> widgets;

    public int getWidth() {
        return width > 0 ? width : 50;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public List<DashboardWidget> getWidgets() {
        if (widgets == null) {
            widgets = new ArrayList<>();
        }
        return widgets;
    }

    public void setWidgets(List<DashboardWidget> widgets) {
        this.widgets = widgets;
    }

    @Override
    public String getLabel() {
        return getWidgets().size() + " Widgets";
    }
}
