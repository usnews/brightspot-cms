<%@ page import="

com.psddev.cms.db.History,
com.psddev.cms.tool.Widget,
com.psddev.cms.tool.JspWidget,

com.psddev.dari.db.Query,
com.psddev.dari.db.State,

com.psddev.dari.util.PaginatedResult,

com.psddev.cms.tool.ToolPageContext,

java.util.List,
java.util.UUID
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);
if (JspWidget.isUpdating(wp)) {
    return;
}

Widget widget = JspWidget.getWidget(wp);
Object object = JspWidget.getObject(wp);

UUID objectId = State.getInstance(object).getId();
History overlaidHistory = wp.getOverlaidHistory(object);
List<History> namedHistories = Query
        .from(History.class)
        .where("objectId = ?", objectId)
        .and("name != missing")
        .sortAscending("name")
        .select();
PaginatedResult<History> histories = History
        .findByObjectId(wp.getUser(), objectId, 0, 10);
if (histories == null || histories.getCount() == 0) {
    return;
}

// --- Presentation ---

%><% if (namedHistories.size() > 0) { %>
    <h2>Named</h2>
    <ul class="links">
        <% for (History history : namedHistories) { %>
            <li<%= history.equals(overlaidHistory) ? " class=\"selected\""
                    : "" %>><a href="<%= wp.objectUrl(null, history)
                    %>"><%= wp.h(history.getName()) %></a></li>
        <% } %>
    </ul>
    <h2>Recent</h2>
<% } %>
<ul class="links pageThumbnails">
    <% for (History history : histories.getItems()) { %>
        <li<%= history.equals(overlaidHistory) ? " class=\"selected\""
                : "" %> data-preview-url="/_preview?_cms.db.previewId=<%= history.getId() %>"><a href="<%= wp.objectUrl(null, history) %>"><%=
                history.getUpdateDate() %> by <%=
                wp.objectLabel(history.getUpdateUser()) %></a></li>
    <% } %>
</ul>
