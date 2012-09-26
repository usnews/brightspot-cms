<%@ page import="

com.psddev.cms.db.Template,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.Database,
com.psddev.dari.db.ObjectType
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);
Template selectedTemplate = Database.Static.findById(wp.getDatabase(), Template.class, wp.uuidParam("templateId"));

// --- Presentation ---

%><% wp.writeHeader(); %>

<h1>Types</h1>
<ul class="links">
    <% for (ObjectType type : selectedTemplate.getContentTypes()) { %>
        <li><a href="<%= wp.url("/content/edit.jsp",
                "templateId", selectedTemplate.getId(),
                "typeId", type.getId()
                ) %>" target="_top"><%= wp.objectLabel(type) %></a></li>
    <% } %>
</ul>

<% wp.writeFooter(); %>
