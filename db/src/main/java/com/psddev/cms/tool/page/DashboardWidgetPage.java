package com.psddev.cms.tool.page;

import java.io.IOException;

import javax.servlet.ServletException;

import com.psddev.cms.db.Content;
import com.psddev.cms.db.Site;
import com.psddev.cms.tool.Dashboard;
import com.psddev.cms.tool.DashboardWidget;
import com.psddev.cms.tool.PageServlet;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.db.Query;
import com.psddev.dari.util.RoutingFilter;
import com.psddev.dari.util.StringUtils;

@RoutingFilter.Path(application = "cms", value = "dashboardWidget")
@SuppressWarnings("serial")
public class DashboardWidgetPage extends PageServlet {

    @Override
    protected String getPermissionId() {
        return null;
    }

    @Override
    protected void doService(final ToolPageContext page) throws IOException, ServletException {
        String[] p = StringUtils.removeStart(page.getRequest().getPathInfo(), "/").split("/");
        String dashboardName = p.length > 0 ? p[0] : null;
        String widgetName = p.length > 1 ? p[1] : null;
        Dashboard dashboard = dashboardName != null ? Query.from(Dashboard.class).where("cms.dashboard.name = ?", dashboardName).first() : null;
        DashboardWidget widget = dashboard != null && widgetName != null ? dashboard.as(Dashboard.Data.class).getWidgetByName(widgetName) : null;

        if (widget == null) {
            page.redirect("/cms", "reason", "no-object");
            return;
        }

        if (!page.hasPermission(widget.as(DashboardWidget.Data.class).getPermissionId(dashboard))) {
            page.redirect("/", "reason", "no-permission-" + widget.as(DashboardWidget.Data.class).getPermissionId(dashboard));
            return;
        }

        Site site = page.getSite();
        if (site != null && widget instanceof Content) {
            if (!((Content) widget).is(site.itemsPredicate())) {
                return;
            }
        }

        widget.writeHtml(page, dashboard);
    }
}
