<%@ page import="

com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.util.HtmlWriter
" %><%

ToolPageContext wp = new ToolPageContext(pageContext);

if (wp.requirePermission("area/dashboard")) {
    return;
}

wp.include("/WEB-INF/header.jsp");

HtmlWriter writer = new HtmlWriter(out);
String[][] dashboardWidgets = new String[][] {
        new String[] { "/misc/siteMap.jsp", "/misc/recentActivity.jsp" },
        new String[] { "/misc/pageBuilder.jsp", "/misc/scheduledEvents.jsp", "/misc/unpublishedDrafts.jsp" } };

writer.start("div", "class", "dashboard dashboard-" + dashboardWidgets.length);
    for (String[] jsps : dashboardWidgets) {
        writer.start("div", "class", "dashboard-column");
            for (String jsp : jsps) {
                writer.start("div", "class", "dashboard-cell frame");
                    writer.start("a", "href", wp.url(jsp)).html(jsp).end();
                writer.end();
            }
        writer.end();
    }
writer.end();

wp.include("/WEB-INF/footer.jsp");
%>
