<%@ page import="

java.io.IOException,

java.util.Map,
java.util.Set,
java.util.UUID,

com.psddev.cms.db.Directory,
com.psddev.cms.db.Section,
com.psddev.cms.db.Template,

com.psddev.cms.tool.ToolFilter,

com.psddev.dari.util.ObjectUtils,
com.psddev.dari.util.PaginatedResult,

com.psddev.cms.db.ToolUser,

com.psddev.dari.db.Record,
com.psddev.dari.db.ObjectType,
com.psddev.dari.db.State,
com.psddev.dari.db.Database,
com.psddev.dari.db.Query,

com.psddev.cms.tool.ToolPageContext
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);
if (wp.requirePermission("area/dashboard")) {
    return;
}

Directory selectedDirectory = Query.findById(Directory.class, wp.pageParam(UUID.class, "directoryId", null));
UUID typeId = wp.pageParam(UUID.class, "typeId", null);

PaginatedResult<?> items = null;
Map<Object, Long> counts = null;
if (selectedDirectory != null) {
    items = Query.
            fromType(ObjectType.getInstance(typeId)).
            where(selectedDirectory.itemsPredicate()).
            and(wp.siteItemsPredicate()).
            sortAscending(Directory.PATHS_FIELD).
            select(wp.longParam("offset"), wp.intParam("limit", 20));
            /*
    counts = Query.
            from(Object.class).
            where(Directory.PATHS_FIELD + " ^= ?", selectedDirectory.getRawPath()).
            and(wp.siteItemsPredicate()).
            countBy("typeId");
            */
}

// --- Presentation ---

%><h1>Site Map</h1>

<form action="<%= wp.url(null) %>" class="autoSubmit" method="get">
    <select name="directoryId" style="max-width: 100%;">
        <option value="">- DIRECTORY -</option>
        <% for (Directory directory : Query.from(Directory.class).sortAscending("path").select()) { %>
            <option<%= directory.equals(selectedDirectory) ? " selected" : "" %> value="<%= wp.h(directory.getId()) %>"><%= wp.objectLabel(directory) %></option>
        <% } %>
    </select>

    <% if (counts != null && counts.size() > 1) { %>
        <select name="typeId">
            <option value="">- ALL TYPES -</option>
            <%
            for (Map.Entry<Object, Long> e : counts.entrySet()) {
                ObjectType type = ObjectType.getInstance(ObjectUtils.asUuid(e.getKey()));
                if (type != null) {
                    %>
                    <option value="<%= type.getId() %>"<%= type.getId().equals(typeId) ? " selected" : "" %>><%= wp.objectLabel(type) %> (<%= wp.h(e.getValue()) %>)</option>
                    <%
                }
            }
            %>
        </select>
    <% } %>
</form>

<% if (selectedDirectory != null) { %>
    <% if (!items.hasItems()) { %>
        <div class="message warning">
            <p>No items in <strong><%= wp.h(selectedDirectory.getPath()) %></strong> directory.</p>
        </div>

    <% } else { %>
        <% if (items.hasPrevious() || items.hasNext()) { %>
            <ul class="pagination">
                <% if (items.hasPrevious()) { %>
                    <li class="first"><a href="<%= wp.url("", "offset", items.getFirstOffset()) %>">First</a></li>
                    <li class="previous"><a href="<%= wp.url("", "offset", items.getPreviousOffset()) %>">Previous <%= items.getLimit() %></a></li>
                <% } %>
                <% if (items.hasNext()) { %>
                    <li class="next"><a href="<%= wp.url("", "offset", items.getNextOffset()) %>">Next <%= items.getLimit() %></a></li>
                    <%--
                    <li class="last"><a href="<%= wp.url("", "offset", items.getLastOffset()) %>">Last</a></li>
                    --%>
                <% } %>
            </ul>
        <% } %>

        <table class="links">
            <tbody>
                <%
                int i = 0;
                for (Object item : items.getItems()) {
                    State itemState = State.getInstance(item);
                    %>
                    <tr>
                        <td><%= wp.objectLabel(itemState.getType()) %></td>
                        <td class="main"><a target="_top" href="<%= wp.objectUrl("/content/edit.jsp", item, "directoryId", selectedDirectory.getId(), "offset", items.getOffset() + i) %>"><%= wp.objectLabel(item) %></a></td>
                        <td><ul>
                            <%
                            String prefix = selectedDirectory.getPath();
                            for (Directory.Path pathObject : itemState.as(Directory.ObjectModification.class).getPaths()) {
                                String path = pathObject.getPath();
                                wp.write("<li>");
                                wp.write(wp.h(path.startsWith(prefix) ? path.substring(prefix.length()) : path));
                                wp.write("</li>");
                            }
                            %>
                        </ul></td>
                    </tr>
                    <%
                    ++ i;
                }
                %>
            </tbody>
        </table>
    <% } %>
<% } %>
