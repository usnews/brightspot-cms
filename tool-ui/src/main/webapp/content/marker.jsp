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
            select()) { %>
        <li><a href="<%= wp.objectUrl("", marker) %>"><%= wp.objectLabel(marker) %></a></li>
    <% } %>
</ul>

<%
Object object = Database.Static.findById(wp.getDatabase(), Object.class, wp.uuidParam("id"));
if (object != null) {
    String pageId = wp.createId();
    State state = State.getInstance(object);
    Reference ref = new Reference();

    ref.as(RichTextReference.class).setLabel(state.getLabel());
    ref.setObject(object);
    %>
    <div id="<%= pageId %>"></div>
    <script type="text/javascript">
        if (typeof jQuery !== 'undefined') (function($) {
            var $page = $('#<%= pageId %>');
            var $source = $page.popup('source');
            $source.rte('enhancement', {
                'reference': '<%= wp.js(ObjectUtils.toJson(ref.getState().getSimpleValues())) %>',
                'id': '<%= state.getId() %>',
                'label': '<%= wp.js(state.getLabel()) %>'
            });
            $page.popup('close');
        })(jQuery);
    </script>
<% } %>
