<%@ page import="

com.psddev.cms.db.Template,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.Query,
com.psddev.dari.db.State,
com.psddev.dari.util.StringUtils
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);
if (wp.requireUser()) {
    return;
}

Object selected = Query.findById(Object.class, wp.uuidParam("id"));

// --- Presentation ---

%><% wp.include("/WEB-INF/header.jsp"); %>

<h1>Advanced Options</h1>
<ul class="links">
    <% if (selected != null) { %>
        <li><a href="<%= wp.objectUrl("/content/raw.jsp", selected) %>" target="_blank">View raw data</a></li>
    <% } %>

    <li>
        <a href="<%= wp.h(StringUtils.addQueryParameters(
                wp.param(String.class, "returnUrl"),
                "deprecated", true)) %>" target="_top">Show deprecated fields</a>
    </li>
</ul>

<% wp.include("/WEB-INF/footer.jsp"); %>
