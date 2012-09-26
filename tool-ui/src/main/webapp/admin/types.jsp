<%@ page import="

com.psddev.cms.db.Seo,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.State,
com.psddev.dari.db.ObjectType,
com.psddev.dari.util.ObjectUtils
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);
String queryString = wp.param("query");
ObjectType selected = (ObjectType) wp.findOrReserve(ObjectType.class);
if (selected != null && !State.getInstance(selected).isNew()) {
    selected = ObjectType.getInstance(selected.getId());
}

State selectedState = State.getInstance(selected);
if (wp.include("/WEB-INF/updateObject.jsp", "object", selected)) {
    return;
}

// --- Presentation ---

%><% wp.include("/WEB-INF/header.jsp"); %>

<div class="withLeftNav">
    <div class="leftNav">
        <div class="widget">

            <h1>Types</h1>
            <ul class="links">
                <li class="new<%= selectedState.isNew() ? " selected" : "" %>">
                    <a href="<%= wp.typeUrl(null, ObjectType.class) %>">New Type</a>
                </li>
            </ul>

            <form action="<%= wp.url("/admin/typesResult.jsp") %>" class="autoSubmit" method="get" target="typesResult">
                <input name="id" type="hidden" value="<%= selectedState.getId() %>">
                <div class="searchInput">
                    <label for="<%= wp.createId() %>">Search</label>
                    <input id="<%= wp.getId() %>" class="autoFocus" name="query" type="text" value="<%= wp.h(queryString) %>">
                    <input type="submit" value="Go">
                </div>
            </form>

            <div class="frame" name="typesResult">
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
