package com.psddev.cms.tool;

import java.util.ArrayList;
import java.util.List;

import com.psddev.cms.tool.widget.BulkUploadWidget;
import com.psddev.cms.tool.widget.CreateNewWidget;
import com.psddev.cms.tool.widget.RecentActivityWidget;
import com.psddev.cms.tool.widget.ResourcesWidget;
import com.psddev.cms.tool.widget.ScheduledEventsWidget;
import com.psddev.cms.tool.widget.SiteMapWidget;
import com.psddev.cms.tool.widget.UnpublishedDraftsWidget;
import com.psddev.cms.tool.widget.WorkStreamsWidget;
import com.psddev.dari.db.Record;

@Dashboard.Embedded
public class Dashboard extends Record {

    private List<DashboardColumn> columns;

    public static Dashboard getDefaultDashboard() {
        Dashboard dashboard = new Dashboard();
        List<DashboardColumn> columns = new ArrayList<>();

        dashboard.setColumns(columns);

        DashboardColumn leftColumn = new DashboardColumn();

        leftColumn.setWidth(60);
        columns.add(leftColumn);

        List<DashboardWidget> leftColumnWidgets = leftColumn.getWidgets();

        leftColumnWidgets.add(new WorkStreamsWidget());
        leftColumnWidgets.add(new RecentActivityWidget());
        leftColumnWidgets.add(new SiteMapWidget());

        DashboardColumn rightColumn = new DashboardColumn();

        rightColumn.setWidth(40);
        columns.add(rightColumn);

        List<DashboardWidget> rightColumnWidgets = rightColumn.getWidgets();

        rightColumnWidgets.add(new CreateNewWidget());
        rightColumnWidgets.add(new BulkUploadWidget());
        rightColumnWidgets.add(new ScheduledEventsWidget());
        rightColumnWidgets.add(new ResourcesWidget());
        rightColumnWidgets.add(new UnpublishedDraftsWidget());

        return dashboard;
    }

    public List<DashboardColumn> getColumns() {
        if (columns == null) {
            columns = new ArrayList<>();
        }
        return columns;
    }

    public void setColumns(List<DashboardColumn> columns) {
        this.columns = columns;
    }
}
