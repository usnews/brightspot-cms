<%@ page import="

com.psddev.cms.db.Site,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.Query,
com.psddev.dari.db.State,

java.util.UUID
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);
if (wp.requirePermission("area/admin/adminSites")) {
    return;
}

String queryString = wp.param("query");
Object selected = wp.findOrReserve(Site.class);
State selectedState = State.getInstance(selected);
if (wp.include("/WEB-INF/updateObject.jsp", "object", selected)) {
    return;
}

// --- Presentation ---

%><% wp.include("/WEB-INF/header.jsp"); %>

<div class="withLeftNav">
    <div class="leftNav">
        <div class="widget">

            <h1>Sites</h1>
            <ul class="links">
                <li class="new<%= selectedState.isNew() ? " selected" : "" %>">
                    <a href="<%= wp.url(null) %>">New Site</a>
                </li>
            </ul>

            <form action="<%= wp.url("/admin/sitesResult.jsp") %>" class="autoSubmit" method="get" target="sitesResult">
                <input name="id" type="hidden" value="<%= selectedState.getId() %>">
                <div class="searchInput">
                    <label for="<%= wp.createId() %>">Search</label>
                    <input id="<%= wp.getId() %>" class="autoFocus" name="query" type="text" value="<%= wp.h(queryString) %>">
                    <input type="submit" value="Go">
                </div>
            </form>

            <div class="frame" name="sitesResult">
            </div>

        </div>
    </div>
    <div class="main">
        <div class="widget">

            <% wp.include("/WEB-INF/editObject.jsp", "object", selected); %>

        </div>
    </div>
</div>

<% wp.include("/WEB-INF/footer.jsp"); %>
