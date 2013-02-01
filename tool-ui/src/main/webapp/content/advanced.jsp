<%@ page import="

com.psddev.cms.db.Template,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.Query,
com.psddev.dari.db.State
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);
if (wp.requireUser()) {
    return;
}

Object selected = Query.findById(Object.class, wp.uuidParam("id"));
State selectedState = State.getInstance(selected);
Template selectedTemplate = selectedState.as(Template.ObjectModification.class).getDefault();

// --- Presentation ---

%><% wp.include("/WEB-INF/header.jsp"); %>

<h1>Advanced Options</h1>
<ul class="links">
    <li><a href="<%= wp.objectUrl("/content/raw.jsp", selected) %>" target="contentRaw">View raw data</a></li>
</ul>

<% wp.include("/WEB-INF/footer.jsp"); %>
