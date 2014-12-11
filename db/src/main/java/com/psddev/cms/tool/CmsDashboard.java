package com.psddev.cms.tool;

import com.psddev.cms.db.Content;
import com.psddev.cms.tool.widgets.CmsWidget;

import java.util.List;

public class CmsDashboard extends Content implements Dashboard {

    @Required
    private String title;

    List<CmsWidget> widgets;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public List<CmsWidget> getWidgets() {
        return widgets;
    }

    public void setWidgets(List<CmsWidget> widgets) {
        this.widgets = widgets;
    }
}
