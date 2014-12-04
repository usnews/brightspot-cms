package com.psddev.cms.tool.page;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

import com.psddev.cms.tool.Dashboard;
import com.psddev.cms.tool.JspWidget;
import com.psddev.cms.tool.PageServlet;
import com.psddev.cms.tool.PageWidget;
import com.psddev.cms.tool.Tool;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.cms.tool.Widget;
import com.psddev.dari.db.Query;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.RoutingFilter;
import com.psddev.dari.util.StringUtils;
import com.psddev.dari.util.TypeReference;

@RoutingFilter.Path(application = "cms", value = "dashboard")
@SuppressWarnings("serial")
public class DashboardPage extends PageServlet {

    @Override
    protected String getPermissionId() {
        return null;
    }

    @Override
    protected void doService(final ToolPageContext page) throws IOException, ServletException {
        String[] p = StringUtils.removeStart(page.getRequest().getPathInfo(), "/").split("/");
        String dashboardName = p.length > 0 ? p[0] : null;
        Dashboard dashboard = dashboardName != null ? Query.from(Dashboard.class).where("cms.dashboard.name = ?", dashboardName).first() : null;

        if (dashboard == null) {
            page.redirect("/", "reason", "no-object");
            return;
        }

        if (!page.hasPermission(dashboard.as(Dashboard.Data.class).getPermissionId())) {
            page.redirect("/", "reason", "no-permission-" + dashboard.as(Dashboard.Data.class).getPermissionId());
            return;
        }

        dashboard.writeHeaderHtml(page);
        writeDashboardWidgets(page, dashboard);
        dashboard.writeFooterHtml(page);
    }

    private static final TypeReference<List<String>> LIST_STRING_TYPE_REFERENCE = new TypeReference<List<String>>() { };
    private static final TypeReference<List<List<String>>> LIST_LIST_STRING_TYPE_REFERENCE = new TypeReference<List<List<String>>>() { };

    private void writeDashboardWidgets(final ToolPageContext page, final Dashboard dashboard) throws IOException {
        List<List<Widget>> widgetsByColumn = Tool.Static.getWidgets(dashboard.as(Dashboard.Data.class).getWidgetPosition());
        List<String> collapse = ObjectUtils.to(LIST_STRING_TYPE_REFERENCE, page.getUser().getState().get(dashboard.as(Dashboard.Data.class).getName() + ".dashboardWidgetsCollapse"));
        List<List<String>> namesByColumn = ObjectUtils.to(LIST_LIST_STRING_TYPE_REFERENCE, page.getUser().getState().get(dashboard.as(Dashboard.Data.class).getName() + ".dashboardWidgets"));

        if (namesByColumn != null) {
            Map<String, Widget> widgetsByName = new LinkedHashMap<String, Widget>();

            for (List<Widget> widgets : widgetsByColumn) {
                for (Widget widget : widgets) {
                    widgetsByName.put(widget.getInternalName(), widget);
                }
            }

            widgetsByColumn = new ArrayList<List<Widget>>();

            for (List<String> names : namesByColumn) {
                List<Widget> widgets = new ArrayList<Widget>();

                widgetsByColumn.add(widgets);

                for (String name : names) {
                    Widget widget = widgetsByName.remove(name);

                    if (widget != null) {
                        widgets.add(widget);
                    }
                }
            }

            if (!widgetsByName.isEmpty()) {
                List<Widget> widgets;

                if (widgetsByColumn.isEmpty()) {
                    widgets = new ArrayList<Widget>();

                    widgetsByColumn.add(widgets);

                } else {
                    widgets = widgetsByColumn.get(widgetsByColumn.size() - 1);
                }

                for (Widget widget : widgetsByName.values()) {
                    widgets.add(widget);
                }
            }
        }

        page.writeStart("div", "class", "dashboard", "data-columns", widgetsByColumn.size(), "data-settings-prefix", dashboard.as(Dashboard.Data.class).getName() + ".");
            for (List<Widget> widgets : widgetsByColumn) {
                page.writeStart("div", "class", "dashboardColumn");
                    for (Widget widget : widgets) {
                        if (!page.hasPermission(widget.getPermissionId())) {
                            continue;
                        }

                        String jsp = null;

                        if (widget instanceof JspWidget) {
                            jsp = ((JspWidget) widget).getJsp();

                        } else if (widget instanceof PageWidget) {
                            jsp = ((PageWidget) widget).getPath();
                        }

                        if (jsp != null) {
                            String name = widget.getInternalName();
                            StringBuilder url = new StringBuilder(page.toolUrl(widget.getTool(), jsp));

                            if (url.toString().contains("?")) {
                                url.append("&");
                            } else {
                                url.append("?");
                            }
                            url.append(page.getRequest().getQueryString());

                            page.writeStart("div",
                                    "class", "dashboardCell" + (collapse != null && collapse.contains(name) ? " dashboardCell-collapse" : ""),
                                    "data-widget", name);
                                page.writeStart("div", "class", "frame");
                                    page.writeStart("a", "href", url).writeHtml(jsp).writeEnd();
                                page.writeEnd();
                            page.writeEnd();
                        }
                    }
                page.writeEnd();
            }
        page.writeEnd();
    }
}
