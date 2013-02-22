<%@ page import="

com.psddev.cms.db.Content,
com.psddev.cms.db.Directory,
com.psddev.cms.db.Page,
com.psddev.cms.db.Template,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.ObjectType,
com.psddev.dari.db.Query,
com.psddev.dari.db.State
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);

if (wp.requirePermission("area/dashboard")) {
    return;
}

// --- Presentation ---

%><div class="widget upload-droppable">

<h1 class="icon icon-file">Page Builder</h1>
<ul class="links pageThumbnails">

    <%
    for (Template template : Query.
            from(Template.class).
            where(wp.siteItemsPredicate()).
            sortAscending("name").
            select()) {
        State itemState = State.getInstance(Query.from(Object.class).where("cms.template.default = ?", template).first());
        String itemPermalink = null;

        if (itemState != null) {
            itemPermalink = itemState.as(Directory.ObjectModification.class).getPermalink();
        }
        %>

        <li data-preview-url="<%= wp.h(itemPermalink) %>">
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
</ul>

</div>
