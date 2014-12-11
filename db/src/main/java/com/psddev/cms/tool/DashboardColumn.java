package com.psddev.cms.tool;

import java.util.ArrayList;
import java.util.List;

import com.psddev.dari.db.Record;
import com.psddev.dari.db.Recordable.Embedded;
import com.psddev.dari.db.State;

@Embedded
public class DashboardColumn<T extends DashboardWidget> extends Record {

    @Embedded
    private List<T> widgets;

    public List<T> getWidgets() {
        if (widgets == null) {
            widgets = new ArrayList<T>();
        }
        return widgets;
    }

    @Override
    public String getLabel() {
        if (!getWidgets().isEmpty()) {
            Object obj = getWidgets().get(0);
            if (obj != null) {
                State widgetState = State.getInstance(obj);
                if (widgetState != null) {
                    return widgetState.getLabel() + " ...";
                }
            }
        }
        return "Column";
    }
}
