<%@ page import="

com.psddev.cms.tool.Tool,
com.psddev.cms.tool.ToolPageContext,
com.psddev.cms.tool.Widget,

com.psddev.dari.db.Query,
com.psddev.dari.util.HtmlWriter,

java.util.List
" %><%

ToolPageContext wp = new ToolPageContext(pageContext);

if (wp.requireUser()) {
    return;
}

Object object = Query.findById(Object.class, wp.uuidParam("id"));
String url = wp.param("url");
List<Widget> widgets = null;
HtmlWriter writer = new HtmlWriter(out);

for (List<Widget> item : wp.getTool().findWidgets(Tool.CONTENT_BOTTOM_WIDGET_POSITION)) {
    widgets = item;
}

wp.include("/WEB-INF/header.jsp");

writer.start("style", "type", "text/css");
    writer.write("body { background-color: transparent; }");
    writer.write(".toolHat, .toolHeader, .toolFooter { display: none; }");
writer.end();

writer.start("div", "class", "remoteOverlay-hat");

    writer.start("h1");
        writer.start("a", "href", wp.url("/"), "target", "_blank");
            writer.start("span", "class", "companyName").html(wp.getCmsTool().getCompanyName()).end();
            writer.html(" CMS");
        writer.end();
    writer.end();

    if (object != null) {
        writer.start("ul", "class", "remoteOverlay-tools");

            writer.start("li");
                writer.start("a",
                        "class", "action-editMain",
                        "href", wp.objectUrl("/content/remoteOverlayEdit.jsp", object),
                        "target", "contentRemoteOverlayEdit");
                    writer.html(wp.typeLabel(object));
                    writer.html(": ");
                    writer.html(wp.objectLabel(object));
                writer.end();
            writer.end();

            if (!widgets.isEmpty()) {
                for (Widget widget : widgets) {
                    writer.start("li");
                        writer.start("a",
                                "class", "action-widget-" + widget.getInternalName(),
                                "href", wp.objectUrl("/content/remoteWidget.jsp", object, "widgetId", widget.getId()),
                                "target", "contentRemoteWidget");
                            writer.html(wp.objectLabel(widget));
                        writer.end();
                    writer.end();
                }
            }

        writer.end();
    }

writer.end();

wp.include("/WEB-INF/footer.jsp");
%>
