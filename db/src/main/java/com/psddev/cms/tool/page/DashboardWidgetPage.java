package com.psddev.cms.tool.page;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import javax.servlet.ServletException;

import com.psddev.cms.db.Site;
import com.psddev.cms.tool.Dashboard;
import com.psddev.cms.tool.DashboardWidget;
import com.psddev.cms.tool.PageServlet;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.db.Query;
import com.psddev.dari.util.RoutingFilter;
import com.psddev.dari.util.StringUtils;
import com.psddev.dari.util.UuidUtils;

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
        UUID dashboardId = p.length > 0 ? UuidUtils.fromString(p[0]) : null;
        UUID widgetId = p.length > 1 ? UuidUtils.fromString(p[1]) : null;
        Dashboard dashboard = dashboardId != null ? Query.findById(Dashboard.class, dashboardId) : null;
        DashboardWidget widget = dashboard != null && widgetId != null ? dashboard.as(Dashboard.Data.class).getWidgetById(widgetId) : null;

        if (widget == null) {
            page.redirect("/cms", "reason", "no-object");
            return;
        }

        if (!page.hasPermission(widget.as(DashboardWidget.Data.class).getPermissionId(dashboard))) {
            page.redirect("/", "reason", "no-permission-" + widget.as(DashboardWidget.Data.class).getPermissionId(dashboard));
            return;
        }

        Site site = page.getSite();
        Site.ObjectModification objectModification = widget.as(Site.ObjectModification.class);
        Set<Site> consumerSites = objectModification.getConsumers();
        if (consumerSites != null && site != null && !consumerSites.contains(site)) {
            page.redirect("/", "reason", "site-not-permitted");
            return;
        }

        widget.writeHtml(page, dashboard);
    }
}
