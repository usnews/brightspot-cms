<%@ page import="

com.psddev.cms.tool.Area,
com.psddev.cms.tool.CmsTool,
com.psddev.cms.tool.JspWidget,
com.psddev.cms.tool.PageWidget,
com.psddev.cms.tool.Tool,
com.psddev.cms.tool.ToolPageContext,
com.psddev.cms.tool.Widget,

com.psddev.dari.util.HtmlWriter,
com.psddev.dari.util.Settings,

java.util.ArrayList,
java.util.LinkedHashMap,
java.util.List,
java.util.Map,
javax.servlet.http.HttpServletResponse
" %><%

ToolPageContext wp = new ToolPageContext(pageContext);

if (wp.requireUser()) {
    return;
}

if (!wp.hasPermission("area/dashboard")) {
    for (Area top : Tool.Static.getTopAreas()) {
        if (wp.hasPermission(top.getPermissionId())) {
            wp.redirect(wp.toolUrl(top.getTool(), top.getUrl()));
            return;
        }
    }

    response.sendError(Settings.isProduction() ?
            HttpServletResponse.SC_NOT_FOUND :
            HttpServletResponse.SC_FORBIDDEN);
    return;
}

wp.include("/WEB-INF/header.jsp");

HtmlWriter writer = new HtmlWriter(out);
List<List<Widget>> widgetsByColumn = Tool.Static.getWidgets(CmsTool.DASHBOARD_WIDGET_POSITION);
List<List<String>> namesByColumn = (List<List<String>>) wp.getUser().getState().get("dashboardWidgets");

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
        List<Widget> widgets = widgetsByColumn.get(widgetsByColumn.size() - 1);

        for (Widget widget : widgetsByName.values()) {
            widgets.add(widget);
        }
    }
}

writer.start("div", "class", "dashboard", "data-columns", widgetsByColumn.size());
    for (List<Widget> widgets : widgetsByColumn) {
        writer.start("div", "class", "dashboardColumn");
            for (Widget widget : widgets) {
                if (!wp.hasPermission(widget.getPermissionId())) {
                    continue;
                }

                String jsp = null;

                if (widget instanceof JspWidget) {
                    jsp = ((JspWidget) widget).getJsp();

                } else if (widget instanceof PageWidget) {
                    jsp = ((PageWidget) widget).getPath();
                }

                if (jsp != null) {
                    writer.start("div", "class", "dashboardCell", "data-widget", widget.getInternalName());
                        writer.start("div", "class", "frame");
                            writer.start("a", "href", wp.toolUrl(widget.getTool(), jsp)).html(jsp).end();
                        writer.end();
                    writer.end();
                }
            }
        writer.end();
    }
writer.end();

wp.include("/WEB-INF/footer.jsp");
%>
