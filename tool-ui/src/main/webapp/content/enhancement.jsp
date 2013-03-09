<%@ page import="

com.psddev.cms.db.ToolUi,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.ObjectType,
com.psddev.dari.db.Query,
com.psddev.dari.db.State,

java.util.HashSet,
java.util.Set,
java.util.UUID
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);
if (wp.requireUser()) {
    return;
}

String pageId = wp.createId();

Object object = wp.findOrReserve();
State state = State.getInstance(object);

UUID typeId = wp.uuidParam("typeId");
if (typeId == null && (state == null || state.isNew())) {
    object = null;
}

if (state != null && wp.isFormPost()) {
    try {
        wp.include("/WEB-INF/objectPost.jsp", "object", object);
        wp.publish(object);
    } catch (Exception ex) {
        wp.getErrors().add(ex);
    }
}

// --- Presentation ---

if (object == null) {
    Set<UUID> validTypeIds = new HashSet<UUID>();
    for (ObjectType type : wp.getDatabase().readList(Query.from(ObjectType.class))) {
        if (type.as(ToolUi.class).isReferenceable()) {
            validTypeIds.add(type.getId());
        }
    }
    wp.include("/WEB-INF/search.jsp"
            , "newJsp", "/content/enhancement.jsp"
            , "resultJsp", "/content/enhancementResult.jsp"
            , "validTypeIds", validTypeIds.toArray(new UUID[validTypeIds.size()])
            );

} else {
    wp.include("/WEB-INF/objectHeading.jsp", "object", object);
    %>

    <form action="<%= wp.url("", "typeId", state.getTypeId(), "id", state.getId()) %>" enctype="multipart/form-data" id="<%= pageId %>" method="post">
        <p><a class="action action-change" href="<%= wp.url("", "typeId", null, "id", null) %>">Change Enhancement</a></p>
        <% wp.include("/WEB-INF/errors.jsp"); %>
        <% wp.include("/WEB-INF/objectForm.jsp", "object", object); %>
        <div class="buttons">
            <button class="action action-save">Save</button>
        </div>
    </form>

    <script type="text/javascript">
        if (typeof jQuery !== 'undefined') (function($) {
            var $source = $('#<%= pageId %>').popup('source');
            var href = $source.attr('href');
            href = href.replace(/([?&])id=[^&]*/, '$1');
            href += '&id=<%= state.getId() %>';
            $source.attr('href', href);
            $source.rte('enhancement', {
                'id': '<%= state.getId() %>',
                'label': '<%= wp.js(state.getLabel()) %>'
            });
        })(jQuery);
    </script>
<% } %>
