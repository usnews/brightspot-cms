package com.psddev.cms.tool.page;

import java.io.IOException;

import javax.servlet.ServletException;

import com.psddev.cms.tool.Dashboard;
import com.psddev.cms.tool.DashboardColumn;
import com.psddev.cms.tool.DashboardWidget;
import com.psddev.cms.tool.PageServlet;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.db.Query;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.RoutingFilter;
import com.psddev.dari.util.StringUtils;

@RoutingFilter.Path(application = "cms", value = "/dashboardWidget")
public class DashboardWidgetPage extends PageServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected String getPermissionId() {
        return null;
    }

    @Override
    protected void doService(ToolPageContext page) throws IOException, ServletException {
        String pathInfo = page.getRequest().getPathInfo();
        pathInfo = StringUtils.removeStart(pathInfo, "/");
        pathInfo = StringUtils.removeEnd(pathInfo, "/");
        String[] pathInfoParts = pathInfo.split("/");

        Dashboard dashboard = Query.
                from(Dashboard.class).
                where("_id = ?", pathInfoParts[0]).
                first();

        if (dashboard == null) {
            dashboard = Dashboard.getDefaultDashboard();
        }

        DashboardColumn column = dashboard.getColumns().get(ObjectUtils.to(int.class, pathInfoParts[1]));
        DashboardWidget widget = column.getWidgets().get(ObjectUtils.to(int.class, pathInfoParts[2]));

        widget.writeHtml(page, dashboard, column);
    }
}
