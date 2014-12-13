package com.psddev.cms.tool.page;

import java.io.IOException;

import javax.servlet.ServletException;

import com.psddev.cms.tool.Dashboard;
import com.psddev.cms.tool.DashboardWidget;
import com.psddev.cms.tool.PageServlet;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.db.Query;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.RoutingFilter;
import com.psddev.dari.util.StringUtils;
import com.psddev.dari.util.TypeDefinition;

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
        Dashboard dashboard;

        switch (pathInfoParts[0]) {
            case "user" :
                dashboard = page.getUser().getDashboard();
                break;

            case "tool" :
                dashboard = page.getCmsTool().getDefaultDashboard();
                break;

            case "default" :
                dashboard = Dashboard.getDefaultDashboard();
                break;

            default :
                throw new IllegalArgumentException();
        }

        DashboardWidget widget = Query.
                from(DashboardWidget.class).
                where("_id = ?", pathInfoParts[1]).
                first();

        if (widget == null) {
            widget = (DashboardWidget) TypeDefinition.getInstance(ObjectUtils.getClassByName(pathInfoParts[1])).newInstance();
        }

        widget.writeHtml(page, dashboard);
    }
}
