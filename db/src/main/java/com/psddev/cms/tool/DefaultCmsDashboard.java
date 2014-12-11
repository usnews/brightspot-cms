package com.psddev.cms.tool;

import java.util.ArrayList;
import java.util.List;

import com.psddev.cms.tool.widget.BulkUploadWidget;
import com.psddev.cms.tool.widget.CmsWidget;
import com.psddev.cms.tool.widget.CreateNewWidget;
import com.psddev.cms.tool.widget.RecentActivityWidget;
import com.psddev.cms.tool.widget.ResourcesWidget;
import com.psddev.cms.tool.widget.ScheduleListWidget;
import com.psddev.cms.tool.widget.SiteMapWidget;
import com.psddev.cms.tool.widget.UnpublishedDraftsWidget;
import com.psddev.cms.tool.widget.WorkStreamsWidget;
import com.psddev.dari.db.Record;
import com.psddev.dari.db.Singleton;

public class DefaultCmsDashboard extends Record implements Dashboard, Singleton {

    private static final String DEFAULT_TITLE = "Default Dashboard";

    @Required
    private String title;

    @Override
    public List<DashboardColumn<CmsWidget>> getColumns() {
        List<DashboardColumn<CmsWidget>> columns = new ArrayList<DashboardColumn<CmsWidget>>();

        DashboardColumn<CmsWidget> leftColumn = new DashboardColumn<CmsWidget>();
        DashboardColumn<CmsWidget> rightColumn = new DashboardColumn<CmsWidget>();
        columns.add(leftColumn);
        columns.add(rightColumn);

        List<CmsWidget> leftColumnWidgets = leftColumn.getWidgets();
        List<CmsWidget> rightColumnWidgets = rightColumn.getWidgets();

        leftColumnWidgets.add(new WorkStreamsWidget());
        leftColumnWidgets.add(new RecentActivityWidget());
        leftColumnWidgets.add(new SiteMapWidget());

        rightColumnWidgets.add(new CreateNewWidget());
        rightColumnWidgets.add(new BulkUploadWidget());
        rightColumnWidgets.add(new ScheduleListWidget());
        rightColumnWidgets.add(new ResourcesWidget());
        rightColumnWidgets.add(new UnpublishedDraftsWidget());

        List<CmsWidget> allWidgets = new ArrayList<CmsWidget>();
        allWidgets.addAll(leftColumnWidgets);
        allWidgets.addAll(rightColumnWidgets);
        for (DashboardWidget widget : allWidgets) {
            widget.as(DashboardWidget.Data.class).setName(widget.getClass().getName());
        }

        return columns;
    }

    @Override
    protected void beforeSave() {
        if (getTitle() == null) {
            setTitle(DEFAULT_TITLE);
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
