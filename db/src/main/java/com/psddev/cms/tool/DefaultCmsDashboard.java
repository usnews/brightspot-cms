package com.psddev.cms.tool;

import com.psddev.cms.db.Content;
import com.psddev.cms.tool.widgets.BulkUploadWidget;
import com.psddev.cms.tool.widgets.CmsWidget;
import com.psddev.cms.tool.widgets.CreateNewWidget;
import com.psddev.cms.tool.widgets.RecentActivityWidget;
import com.psddev.cms.tool.widgets.ResourcesWidget;
import com.psddev.cms.tool.widgets.ScheduleListWidget;
import com.psddev.cms.tool.widgets.SiteMapWidget;
import com.psddev.cms.tool.widgets.UnpublishedDraftsWidget;
import com.psddev.cms.tool.widgets.WorkStreamsWidget;
import com.psddev.dari.db.Singleton;

import java.util.ArrayList;
import java.util.List;

public class DefaultCmsDashboard extends CmsDashboard implements Singleton {

    private static final String DEFAULT_TITLE = "Default Dashboard";

    @Override
    protected void beforeSave() {
        List<CmsWidget> widgets = new ArrayList<CmsWidget>();
        widgets.add(new WorkStreamsWidget());
        widgets.add(new RecentActivityWidget());
        widgets.add(new SiteMapWidget());
        widgets.add(new CreateNewWidget());
        widgets.add(new BulkUploadWidget());
        widgets.add(new ScheduleListWidget());
        widgets.add(new UnpublishedDraftsWidget());
        widgets.add(new ResourcesWidget());

        for (CmsWidget widget : widgets) {
            ((Content) widget).save();
        }

        this.setWidgets(widgets);
        this.setTitle(DEFAULT_TITLE);
    }
}
