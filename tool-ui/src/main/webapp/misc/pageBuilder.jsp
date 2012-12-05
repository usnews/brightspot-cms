<%@ page import="

com.psddev.cms.db.Page,
com.psddev.cms.db.Template,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.ObjectType,
com.psddev.dari.db.Query
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);
if (wp.requirePermission("area/dashboard")) {
    return;
}

// --- Presentation ---

%><div class="widget">

<h1 class="icon-file">Page Builder</h1>
<h2>Create New</h2>
<ul class="links">

    <% for (Template template : Query.
            from(Template.class).
            where(wp.siteItemsPredicate()).
            sortAscending("name").
            select()) { %>

        <li>
            <% if (template.getContentTypes().size() == 1) { %>
                <a href="<%= wp.url("/content/edit.jsp",
                        "templateId", template.getId())
                        %>" id="<%= wp.createId() %>" target="_top"><%= wp.objectLabel(template) %></a>
            <% } else { %>
                <a href="<%= wp.url("/misc/pageBuilderTemplateTypes.jsp",
                        "templateId", template.getId())
                        %>" id="<%= wp.createId() %>" target="pageBuilderTemplateTypes"><%= wp.objectLabel(template) %></a>
            <% } %>
        </li>
    <% } %>

    <li><a href="<%= wp.url("/content/edit.jsp",
            "typeId", ObjectType.getInstance(Page.class).getId())
            %>" id="<%= wp.createId() %>" target="_top">One-off Page</a></li>
</ul>

</div>
