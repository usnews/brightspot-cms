<%@ page import="

java.util.Collection,

com.psddev.cms.db.Directory,

com.psddev.dari.util.PaginatedResult,

com.psddev.cms.db.ToolUser,

com.psddev.dari.db.Record,
com.psddev.dari.db.Database,
com.psddev.dari.db.Query,
com.psddev.dari.db.State,

com.psddev.cms.tool.ToolPageContext
" %><%

// --- logic ---
ToolPageContext wp = new ToolPageContext(pageContext);
if (wp.requireUser()) {
    return;
}

Directory directory = Query.findById(Directory.class, wp.uuidParam("id"));
Record record = Query.findById(Record.class, wp.uuidParam("recordId"));

int limit = 10;
long offset = wp.longParam("offset");
offset -= limit / 2;
if (offset < 0) {
    offset = 0;
}
PaginatedResult<Object> items = Query
        .from(Object.class)
        .where(Directory.PATHS_FIELD + " ^= ?", directory.getRawPath())
        .select(offset, limit);

// --- presentation ---
%><% wp.include("/WEB-INF/header.jsp"); %>

<h1><strong><%= items.getCount() %></strong> Items</h1>

<%--p style="float: left;"><a target="_top" href="<%=
        wp.url("/content/edit.jsp", "id", directory.getId(),
        "itemId", record.getId(), "offset", offset)
        %>">Edit All Together</a></p--%>

<ul class="pagination">
    <% if (items.hasPrevious()) { %>
        <li class="previous"><a href="<%= wp.url("",
                "offset", items.getPreviousOffset())
                %>">Previous <%= items.getLimit() %></a></li>
    <% } %>
    <% if (items.hasNext()) { %>
        <li class="next"><a href="<%= wp.url("",
                "offset", items.getNextOffset())
                %>">Next <%= items.getLimit() %></a></li>
    <% } %>
</ul>

<ul class="links">
    <% int i = 0; for (Object r : items.getItems()) { %>
        <li<%= r.equals(record) ? " class=\"selected\"" : "" %>>
            <a target="_top" href="<%= wp.url(
                    "/content/edit.jsp", "id", State.getInstance(r).getId(),
                    "directoryId", directory.getId(),
                    "offset", items.getOffset() + i)
                    %>"><%= wp.h(State.getInstance(r).as(Directory.ObjectModification.class).getPermalink()
                    .substring(directory.getPath().length())) %></a>
        </li>
    <% i ++; } %>
</ul>

<% wp.include("/WEB-INF/footer.jsp"); %>
