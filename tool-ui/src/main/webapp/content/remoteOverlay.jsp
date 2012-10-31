<%@ page import="

com.psddev.cms.db.Directory,
com.psddev.cms.tool.CmsTool,
com.psddev.cms.tool.ToolPageContext,
com.psddev.cms.tool.Widget,
com.psddev.dari.db.Query,

java.net.URI,
java.util.List
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);
if (wp.requirePermission("area/dashboard")) {
    return;
}

Object object = Query.findById(Object.class, wp.uuidParam("id"));
String url = wp.param("url");

List<Widget> widgets = null;
for (List<Widget> item : wp.getTool().findWidgets(CmsTool.CONTENT_BOTTOM_WIDGET_POSITION)) {
    widgets = item;
}

// --- Presentation ---

%><% wp.include("/WEB-INF/header.jsp"); %>

<style type="text/css">

/* Disable normal tool styles. */
body {
    background-color: transparent;
}
.toolHat,
.toolHeader,
.toolFooter {
    display: none;
}

/* Replacement hat. */
.overlayHat {
    background-color: white;
    -moz-box-shadow: 0 0 10px black;
    -webkit-box-shadow: 0 0 10px black;
    box-shadow: 0 0 10px black;
    color: white;
    padding: 5px 10px;
    position: fixed;
    right: 0;
    top: 0;
    z-index: 1;
}
.overlayHat > h1 > a {
    color: #333;
}
.overlayHat > h1 > a:hover {
    text-decoration: none;
}
.overlayHat > h1,
.overlayHat > ul {
    display: inline-block;
    margin: 0 0 0 10px;
}
.overlayHat > .closeButton {
    background-color: white;
    border: 2px solid #333;
    border-radius: 20px;
    -moz-border-radius: 20px;
    -webkit-border-radius: 20px;
    bottom: -10px;
    color: #333;
    display: inline-block;
    height: 20px;
    left: -12px;
    line-height: 20px;
    padding-left: 1px;
    position: absolute;
    text-align: center;
    width: 20px;
}
.overlayHat > .closeButton:hover {
    text-decoration: none;
}

/* Dark translucent overlay on top of a page. */
.overlay {
    background-color: rgba(68, 68, 68, 0.2);
    border: 1px solid rgba(255, 255, 255, 0.5);
    color: white;
    left: 0;
    padding: 4px;
    position: fixed;
    top: 0;
}
.overlay.hasObject {
    background-color: rgba(68, 68, 68, 0.5);
}
.overlay > .content {
    background-color: rgba(68, 68, 68, 0.8);
    border-radius: 5px;
    -moz-border-radius: 5px;
    -webkit-border-radius: 5px;
    color: white;
    padding: 5px 10px;
}
.overlay.short > .content {
    bottom: 4px;
    left: 4px;
    position: absolute;
    right: 4px;
}
.overlay > .content > h1 {
    display: inline;
    font-size: 100%;
    margin-right: 20px;
}
.overlay > .content > a.editButton {
    background: transparent url(<%= wp.url("/style/icon/pencil.png") %>) no-repeat left center;
    color: #7be;
    padding-left: 20px;
}

/* Edit form popup. */
.popup[name=contentRemoteOverlayEdit] {
    width: 80%;
}
</style>

<div class="overlayHat">
    <h1><a href="<%= wp.url("/") %>" target="_blank">
        <span class="companyName"><%= wp.h(wp.getCmsTool().getCompanyName()) %></span>
        CMS
    </a></h1>

    <% if (object != null) { %>
        <ul class="piped">
            <li><a class="icon-page_edit" href="<%= wp.objectUrl("/content/remoteOverlayEdit.jsp", object) %>" target="contentRemoteOverlayEdit">Main Content (<%= wp.typeLabel(object) %>: <%= wp.objectLabel(object) %>)</a>
        </ul>
    <% } %>

    <% if (!widgets.isEmpty()) { %>
        <ul class="piped">
            <% for (Widget widget : widgets) { %>
                <li><a class="icon-<%= widget.getIconName() %>" href="<%= wp.objectUrl("/content/remoteWidget.jsp", object, "widgetId", widget.getId()) %>" target="contentRemoteWidget"><%= wp.objectLabel(widget) %></a></li>
            <% } %>
        </ul>
    <% } %>

    <a class="closeButton" href="<%= wp.h(url) %>" target="_top">&#x2716;</a>
</div>

<% wp.include("/WEB-INF/footer.jsp"); %>
