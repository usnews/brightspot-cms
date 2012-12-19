<%@ page import="

com.psddev.cms.db.Content,
com.psddev.cms.db.Directory,
com.psddev.cms.tool.Widget,
com.psddev.cms.tool.JspWidget,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.Query,
com.psddev.dari.db.State,

com.psddev.dari.util.PaginatedResult
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);
if (JspWidget.isUpdating(wp)) {
    return;
}

Widget widget = JspWidget.getWidget(wp);
Object object = JspWidget.getObject(wp);
State state = State.getInstance(object);

PaginatedResult<Object> result = Query.
        fromGroup(Content.SEARCHABLE_GROUP).
        where("* matches ?", state.getId().toString()).
        and("id != ?", state.getId()).
        select(wp.longParam("offset"), wp.intParam("limit", 10));

if (!result.hasItems()) {
    return;
}

// --- Presentation ---

%><ul class="pagination">
    <% if (result.hasPrevious()) { %>
        <li class="previous">
            <a href="<%= wp.url("", "offset", result.getPreviousOffset()) %>">Previous <%= result.getLimit() %></a>
        </li>
    <% } %>

    <li class="label">
        <%= result.getFirstItemIndex() %> to
        <%= result.getLastItemIndex() %> of
        <strong><%= result.getCount() %></strong>
    </li>

    <% if (result.hasNext()) { %>
        <li class="next">
            <a href="<%= wp.url("", "offset", result.getNextOffset()) %>">Next <%= result.getLimit() %></a>
        </li>
    <% } %>
</ul>

<ul class="links pageThumbnails">
    <% for (Object item : result.getItems()) { %>
        <li data-preview-url="<%= wp.h(State.getInstance(item).as(Directory.ObjectModification.class).getPermalink()) %>">
            <a href="<%= wp.objectUrl("/content/edit.jsp", item) %>"><%= wp.typeLabel(item) %>: <%= wp.objectLabel(item) %></a>
        </li>
    <% } %>
</ul>
