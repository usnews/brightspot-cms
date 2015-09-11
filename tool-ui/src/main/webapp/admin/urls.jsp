<%@ page session="false" import="

com.psddev.cms.db.Directory,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.Query,
com.psddev.dari.db.State,

com.psddev.dari.util.PaginatedResult,

java.util.UUID
, com.google.common.collect.ImmutableMap" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);
if (wp.requirePermission("area/admin/adminUrls")) {
    return;
}

Directory selected = (Directory) wp.findOrReserve(Directory.class);
State state = State.getInstance(selected);

if (wp.tryStandardUpdate(selected)) {
    return;

} else if (wp.isFormPost()) {
    try {
        String action = wp.param("action");
        if ("Move".equals(action)) {

        } else if ("Copy".equals(action)) {

        } else if ("Remove".equals(action)) {
        }

    } catch (Exception ex) {
        wp.getErrors().add(ex);
    }
}

// --- Presentation ---

PaginatedResult<Object> items = Query
        .from(Object.class)
        .where(Directory.PATHS_FIELD + " ^= ?", selected.getRawPath())
        .select(wp.longParam("offset"), wp.intParam("limit", 10));

%><% wp.include("/WEB-INF/header.jsp"); %>

<div class="withLeftNav">
    <div class="leftNav">

        <div class="widget">
            <h1 class="icon icon-object-directory">URLs</h1>
            <ul class="links">
                <li class="new<%= State.getInstance(selected).isNew() ? " selected" : "" %>">
                    <a href="<%= wp.url(null) %>">
                        <%= wp.h(wp.localize(Directory.class, "action.new.type"))%>
                    </a>
                </li>
                <% for (Directory directory : Query
                        .from(Directory.class)
                        .sortAscending("path")
                        .select()) { %>
                    <li<%= directory.equals(selected) ? " class=\"selected\"" : "" %>>
                        <a href="<%= wp.objectUrl(null, directory) %>"><%= wp.objectLabel(directory) %></a>
                    </li>
                <% } %>
            </ul>
        </div>

    </div>
    <div class="main">

        <% wp.include("/WEB-INF/errors.jsp"); %>

        <div class="widget">
            <% wp.writeStandardForm(selected); %>
        </div>

        <% if (!state.isNew()) { %>
            <div class="widget">

                <h1><strong><%= items.getCount() %></strong> Items</h1>

                <ul class="pagination">
                    <% if (items.hasPrevious()) { %>
                        <li class="previous">
                            <a href="<%= wp.url("",
                                "offset", items.getPreviousOffset())%>">
                                <%= wp.h(wp.localize(
                                        ImmutableMap.of("count", (Object) items.getLimit()),
                                        "pagination.previous.count"))%>
                            </a>
                        </li>
                    <% } %>
                    <% if (items.hasNext()) { %>
                        <li class="next">
                            <a href="<%= wp.url("",
                                "offset", items.getNextOffset())%>">
                                <%= wp.h(wp.localize(
                                        ImmutableMap.of("count", (Object) items.getLimit()),
                                        "pagination.next.count"))%>
                            </a>
                        </li>
                    <% } %>
                </ul>

                <form method="post" action="<%= wp.objectUrl(null, selected) %>">

                    <table class="table-striped"><tbody>
                        <%
                        int i = 0;
                        for (Object item : items.getItems()) {
                            State itemState = State.getInstance(item);
                            UUID itemId = itemState.getId();
                            %>
                            <tr>
                                <td><input type="checkbox" name="itemId"
                                        value="<%= itemId %>" />
                                <td><%= wp.h(itemState.as(Directory.ObjectModification.class).getPermalink()) %></td>
                                <td><a href="<%= wp.url("/content/edit.jsp",
                                        "id", itemId,
                                        "directoryId", selected.getId(),
                                        "offset", items.getOffset() + i) %>"><%=
                                        wp.objectLabel(item) %></a></td>
                            </tr>
                            <%
                            i ++;
                        }
                        %>
                    </tbody></table>

                    <div class="buttons">
                        <input type="text" name="path" />
                        <input type="submit" name="action" value="Move" />
                        <input type="submit" name="action" value="Copy" />
                        <input class="remove" type="submit" name="action" value="Remove" />
                    </div>

                </form>

            </div>
        <% } %>

    </div>
</div>

<% wp.include("/WEB-INF/footer.jsp"); %>
