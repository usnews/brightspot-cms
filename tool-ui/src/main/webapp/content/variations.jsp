<%@ page import="

com.psddev.cms.db.Variation,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.Query,

java.util.UUID
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);
if (wp.requirePermission("area/dashboard")) {
    return;
}

UUID variationId = wp.uuidParam("variationId");

// --- Presentation ---

%><% wp.include("/WEB-INF/header.jsp"); %>

<h1>Variations</h1>
<ul class="links">
    <li<%= variationId == null ? " class=\"selected\"" : "" %>><a href="<%= wp.returnUrl("variationId", null) %>" target="_top">Default</a></li>
    <% for (Variation variation : Query.from(Variation.class).sortAscending("name").select()) { %>
        <li<%= variation.getId().equals(variationId) ? " class=\"selected\"" : "" %>><a href="<%= wp.returnUrl("variationId", variation.getId()) %>" target="_top"><%= wp.objectLabel(variation) %></a></li>
    <% } %>
</ul>

<% wp.include("/WEB-INF/footer.jsp"); %>
