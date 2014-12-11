<%@ page session="false" import="

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
, com.psddev.cms.tool.page.DashboardPage" %><%

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

DashboardPage.reallyDoService(wp);
%>
