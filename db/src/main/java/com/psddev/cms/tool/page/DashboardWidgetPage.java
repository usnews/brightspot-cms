package com.psddev.cms.tool.page;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.ServletException;

import com.psddev.cms.tool.Dashboard;
import com.psddev.cms.tool.DashboardColumn;
import com.psddev.cms.tool.DashboardWidget;
import com.psddev.cms.tool.PageServlet;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.db.Query;
import com.psddev.dari.util.RoutingFilter;

@RoutingFilter.Path(application = "cms", value = "/dashboardWidget")
public class DashboardWidgetPage extends PageServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected String getPermissionId() {
        return null;
    }

    @Override
    protected void doService(ToolPageContext page) throws IOException, ServletException {
        Dashboard dashboard = Query.
                from(Dashboard.class).
                where("_id = ?", page.param(UUID.class, "dashboardId")).
                first();

        if (dashboard == null) {
            dashboard = Dashboard.getDefaultDashboard();
        }

        DashboardColumn column = dashboard.getColumns().get(page.param(int.class, "column"));
        DashboardWidget widget = column.getWidgets().get(page.param(int.class, "widget"));

        widget.writeHtml(page, dashboard, column);
    }
}
