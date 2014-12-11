package com.psddev.cms.tool;

import java.util.ArrayList;
import java.util.List;

import com.psddev.cms.db.Content;
import com.psddev.cms.tool.widget.CmsWidget;

public class CmsDashboard extends Content implements Dashboard {

    @Required
    private String title;

    private List<DashboardColumn<CmsWidget>> columns;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public List<DashboardColumn<CmsWidget>> getColumns() {
        if (columns == null) {
            columns = new ArrayList<DashboardColumn<CmsWidget>>();
        }
        return columns;
    }

}
