<%@ page import="

com.psddev.cms.db.ReferentialTextMarker,
com.psddev.cms.db.RichTextReference,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.Database,
com.psddev.dari.db.Reference,
com.psddev.dari.db.State,
com.psddev.dari.util.ObjectUtils
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);
if (wp.requireUser()) {
    return;
}

// --- Presentation ---

%><h1>Select a Marker</h1>

<ul class="links">
    <% for (ReferentialTextMarker marker : wp
            .queryFrom(ReferentialTextMarker.class)
            .sortAscending("displayName").
            select()) {
        State state = State.getInstance(marker);
        Reference ref = new Reference();

        ref.as(RichTextReference.class).setLabel(state.getLabel());
        ref.setObject(marker);
        %>
        <li><a data-enhancement="<%= wp.h(ObjectUtils.toJson(ref.getState().getSimpleValues())) %>" href="<%= wp.objectUrl("", marker) %>"><%= wp.objectLabel(marker) %></a></li>
    <% } %>
</ul>
