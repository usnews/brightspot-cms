<%@ page import="

com.psddev.cms.db.Content,
com.psddev.cms.db.ToolUser,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.Query,
com.psddev.dari.db.State,

com.psddev.dari.util.DateUtils,
com.psddev.dari.util.ObjectUtils,
com.psddev.dari.util.PaginatedResult,

java.util.Date,
java.util.UUID
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);
if (wp.requirePermission("area/dashboard")) {
    return;
}

Query<Object> query = Query.
        fromGroup(Content.SEARCHABLE_GROUP).
        where(wp.siteItemsPredicate()).
        and(Content.UPDATE_DATE_FIELD + " != missing").
        sortDescending(Content.UPDATE_DATE_FIELD);

UUID userId = wp.uuidParam("userId");
if (userId != null) {
    query.and(Content.UPDATE_USER_FIELD + " = ?", userId);
}

PaginatedResult<Object> result = query.select(wp.longParam("offset"), wp.intParam("limit", 5));

// --- Presentation ---

%><div class="widget">

<style type="text/css">
.widget-recentActivity .userSelector { float: left; }
.widget-recentActivity table { clear: both; margin-top: 12px; }
</style>

<h1 class="icon-list">Recent Activity</h1>
<div class="widget-recentActivity">

    <form action="<%= wp.url(null) %>" class="userSelector autoSubmit" method="get">
        <input<%= userId != null ? " checked" : "" %> id="<%= wp.createId() %>" name="userId" type="radio" value="<%= wp.getUser().getId() %>">
        <label for="<%= wp.getId() %>">Me</label>
        <input<%= userId == null ? " checked" : "" %> id="<%= wp.createId() %>" name="userId" type="radio" value="">
        <label for="<%= wp.getId() %>">Everyone</label>
        <input type="submit" value="Go">
    </form>

    <% if (result.hasPrevious() || result.hasNext()) { %>
        <ul class="pagination">
            <% if (result.hasPrevious()) { %>
                <li class="first"><a href="<%= wp.url("", "offset", result.getFirstOffset()) %>">Most Recent</a></li>
                <li class="previous"><a href="<%= wp.url("", "offset", result.getPreviousOffset()) %>">Previous <%= result.getLimit() %></a></li>
            <% } %>
            <% if (result.hasNext()) { %>
                <li class="next"><a href="<%= wp.url("", "offset", result.getNextOffset()) %>">Next <%= result.getLimit() %></a></li>
            <% } %>
        </ul>
    <% } %>

    <table class="links"><tbody>
        <%
        String oldDate = null;
        for (Object item : result.getItems()) {
            State state = State.getInstance(item);
            Date updateDate = state.as(Content.ObjectModification.class).getUpdateDate();
            String date = DateUtils.toString(updateDate, "MMM dd, yyyy");
            String time = DateUtils.toString(updateDate, "hh:mm a");
            %>
            <tr>
                <td><%
                if (!ObjectUtils.equals(date, oldDate)) {
                    wp.write(wp.h(date));
                    oldDate = date;
                }
                %></td>
                <td><%= wp.h(time) %></td>
                <td><%= wp.typeLabel(item) %></td>
                <td class="main"><a href="<%= wp.objectUrl("/content/edit.jsp", item) %>" target="_top"><%= wp.objectLabel(item) %></a></td>
                <td><%= wp.objectLabel(state.as(Content.ObjectModification.class).getUpdateUser()) %></td>
            </tr>
        <% } %>
    </tbody></table>
</div>

</div>
