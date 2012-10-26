<%@ page import="

com.psddev.cms.db.DraftStatus,
com.psddev.cms.db.Workflow,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.Query,
com.psddev.dari.db.State,

java.util.List
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);
if (wp.requirePermission("area/admin/adminWorkflows")) {
    return;
}

Object selected = wp.findOrReserve(DraftStatus.class, Workflow.class);
Class<?> selectedClass = selected.getClass();
State selectedState = State.getInstance(selected);

if (wp.include("/WEB-INF/updateObject.jsp", "object", selected)) {
    return;
}

List<DraftStatus> statuses = Query.from(DraftStatus.class).sortAscending("name").select();
List<Workflow> workflows = Query.from(Workflow.class).sortAscending("name").select();

// --- Presentation ---

%><% wp.include("/WEB-INF/header.jsp"); %>

<div class="withLeftNav">
    <div class="leftNav">
        <div class="widget">

            <h1>Workflows</h1>

            <h2>Statuses</h2>
            <ul class="links">
                <li class="new<%= selectedClass == DraftStatus.class && selectedState.isNew() ? " selected" : "" %>">
                    <a href="<%= wp.typeUrl(null, DraftStatus.class) %>">New Status</a>
                </li>
                <% for (DraftStatus status : statuses) { %>
                    <li<%= status.equals(selected) ? " class=\"selected\"" : "" %>>
                        <a href="<%= wp.objectUrl(null, status) %>"><%= wp.objectLabel(status) %></a>
                    </li>
                <% } %>
            </ul>

            <h2>Workflows</h2>
            <ul class="links">
                <li class="new<%= selectedClass == Workflow.class && selectedState.isNew() ? " selected" : "" %>">
                    <a href="<%= wp.typeUrl(null, Workflow.class) %>">New Workflow</a>
                </li>
                <% for (Workflow workflow : workflows) { %>
                    <li<%= workflow.equals(selected) ? " class=\"selected\"" : "" %>>
                        <a href="<%= wp.objectUrl(null, workflow) %>"><%= wp.objectLabel(workflow) %></a>
                    </li>
                <% } %>
            </ul>

        </div>
    </div>
    <div class="main">

        <div class="widget">
            <% wp.include("/WEB-INF/editObject.jsp", "object", selected); %>
        </div>

    </div>
</div>

<% wp.include("/WEB-INF/footer.jsp"); %>
