<%@ page import="

com.psddev.cms.tool.CmsTool,
com.psddev.cms.tool.JspWidget,
com.psddev.cms.tool.Tool,
com.psddev.cms.tool.ToolPageContext,
com.psddev.cms.tool.Widget,

com.psddev.dari.util.HtmlWriter,

java.util.ArrayList,
java.util.LinkedHashMap,
java.util.List,
java.util.Map
" %><%

ToolPageContext wp = new ToolPageContext(pageContext);

if (wp.requireUser()) {
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
        writer.start("div", "class", "dashboard_column");
            for (Widget widget : widgets) {
                if (widget instanceof JspWidget) {
                    String jsp = ((JspWidget) widget).getJsp();

                    writer.start("div", "class", "dashboard_cell", "data-widget", widget.getInternalName());
                        writer.start("div", "class", "frame");
                            writer.start("a", "href", wp.url(jsp)).html(jsp).end();
                        writer.end();
                    writer.end();
                }
            }
        writer.end();
    }
writer.end();

wp.include("/WEB-INF/footer.jsp");
%>
