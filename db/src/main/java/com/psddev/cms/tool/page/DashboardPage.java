package com.psddev.cms.tool.page;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;

import com.psddev.cms.db.Site;
import com.psddev.cms.db.ToolRole;
import com.psddev.cms.db.ToolUser;
import com.psddev.cms.tool.CmsTool;
import com.psddev.cms.tool.Dashboard;
import com.psddev.cms.tool.DashboardColumn;
import com.psddev.cms.tool.DashboardWidget;
import com.psddev.cms.tool.PageServlet;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.util.RoutingFilter;

@RoutingFilter.Path(application = "cms", value = "/dashboard")
public class DashboardPage extends PageServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected String getPermissionId() {
        return "area/dashboard";
    }

    @Override
    public void doService(ToolPageContext page) throws IOException, ServletException {
        ToolUser user = page.getUser();
        Dashboard dashboard = page.getUser().getDashboard();

        if (dashboard == null) {
            ToolRole role = user.getRole();

            if (role != null) {
                dashboard = role.getDashboard();
            }
        }

        if (dashboard == null) {
            Site site = page.getSite();

            if (site != null) {
                dashboard = site.getDashboard();
            }
        }

        if (dashboard == null) {
            dashboard = page.getCmsTool().getDefaultDashboard();
        }

        if (dashboard == null) {
            dashboard = Dashboard.getDefaultDashboard();
        }

        page.writeHeader();
            page.writeStart("div", "class", "dashboard-columns");
                List<DashboardColumn> columns = dashboard.getColumns();
                double totalWidth = 0;

                for (DashboardColumn column : columns) {
                    int width = column.getWidth();
                    totalWidth += width > 0 ? width : 1;
                }

                for (int c = 0, cSize = columns.size(); c < cSize; ++ c) {
                    DashboardColumn column = columns.get(c);
                    int width = column.getWidth();

                    page.writeStart("div",
                            "class", "dashboard-column",
                            "style", page.cssString("width", ((width > 0 ? width : 1) / totalWidth * 100) + "%"));

                        List<DashboardWidget> widgets = column.getWidgets();

                        for (int w = 0, wSize = widgets.size(); w < wSize; ++ w) {
                            page.writeStart("div", "class", "frame dashboard-widget");
                                page.writeStart("a", "href", page.toolUrl(CmsTool.class, "/dashboardWidget/" + dashboard.getId() + "/" + c + "/" + w));
                                page.writeEnd();
                            page.writeEnd();
                        }
                    page.writeEnd();
                }
            page.writeEnd();
        page.writeFooter();
    }
}
