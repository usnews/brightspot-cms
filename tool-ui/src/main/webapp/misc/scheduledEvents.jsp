<%@ page import="

com.psddev.cms.db.Content,
com.psddev.cms.db.Draft,
com.psddev.cms.db.Schedule,
com.psddev.cms.db.ToolUser,
com.psddev.cms.tool.ToolFilter,

com.psddev.dari.db.Record,
com.psddev.dari.db.Query,
com.psddev.dari.db.State,
com.psddev.dari.db.ObjectType,

com.psddev.dari.util.DateUtils,
com.psddev.dari.util.ObjectUtils,
com.psddev.dari.util.PaginatedResult,

com.psddev.cms.tool.ToolPageContext,

java.util.Date,
java.util.List,
java.util.UUID
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);
if (wp.requirePermission("area/dashboard")) {
    return;
}

PaginatedResult<Schedule> result = Query
        .from(Schedule.class)
        .sortAscending("triggerDate")
        .select(wp.longParam("offset"), wp.intParam("limit", 10));

// --- Presentation --- 
%><div class="widget">

<h1 class="icon-calendar">Schedules</h1>
<div class="widget-recentActivity">
    <% if (result.hasPrevious() || result.hasNext()) { %>
        <ul class="pagination">
            <% if (result.hasPrevious()) { %>
                <li class="first"><a href="<%= wp.url("",
                        "offset", result.getFirstOffset())
                        %>">Most Recent</a></li>
                <li class="previous"><a href="<%= wp.url("",
                        "offset", result.getPreviousOffset())
                        %>">Previous <%= result.getLimit() %></a></li>
            <% } %>
            <% if (result.hasNext()) { %>
                <li class="next"><a href="<%= wp.url("",
                        "offset", result.getNextOffset())
                        %>">Next <%= result.getLimit() %></a></li>
            <% } %>
        </ul>
    <% } %>

    <ul>
        <% for (Schedule schedule : result.getItems()) { %>
            <% List<Draft> drafts = Query.from(Draft.class)
                    .where("schedule = ?", schedule)
                    .select(); %>
            <% if (drafts.size() > 0) { %>
                <li><%= wp.h(schedule.getTriggerDate()) %> by <%=
                        wp.objectLabel(schedule.getTriggerUser()) %><ul>
                    <% for (Draft draft : drafts) { %>
                        <li><a href="<%= wp.objectUrl("/content/edit.jsp", draft)
                                %>" target="_top"><%= wp.objectLabel(
                                draft.getObject()) %></a></li>
                    <% } %>
                </ul></li>
            <% } %>
        <% } %>
    </ul>
</div>

</div>
