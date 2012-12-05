<%@ page import="

com.psddev.cms.tool.CmsTool,
com.psddev.cms.tool.JspWidget,
com.psddev.cms.tool.Tool,
com.psddev.cms.tool.ToolPageContext,
com.psddev.cms.tool.Widget,

com.psddev.dari.util.HtmlWriter,

java.util.List
" %><%

ToolPageContext wp = new ToolPageContext(pageContext);

if (wp.requirePermission("area/dashboard")) {
    return;
}

wp.include("/WEB-INF/header.jsp");

HtmlWriter writer = new HtmlWriter(out);
List<List<Widget>> dashboardWidgets = Tool.Static.getWidgets(CmsTool.DASHBOARD_WIDGET_POSITION);

writer.start("div", "class", "dashboard dashboard-" + dashboardWidgets.size());
    for (List<Widget> widgets : dashboardWidgets) {
        writer.start("div", "class", "dashboard-column");
            for (Widget widget : widgets) {
                if (widget instanceof JspWidget) {
                    String jsp = ((JspWidget) widget).getJsp();
                    writer.start("div", "class", "dashboard-cell frame");
                        writer.start("a", "href", wp.url(jsp)).html(jsp).end();
                    writer.end();
                }
            }
        writer.end();
    }
writer.end();

wp.include("/WEB-INF/footer.jsp");
%>
