<%@ page import="

com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.State,

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

%><% if (object == null) { %>
    <% wp.include("/WEB-INF/search.jsp",
            "newJsp", "/content/sectionContent.jsp",
            "resultJsp", "/content/sectionContentResult.jsp"); %>

<% } else { %>
    <% wp.include("/WEB-INF/objectHeading.jsp", "object", object); %>

    <form action="<%= wp.url("", "typeId", state.getTypeId(), "id", state.getId()) %>" enctype="multipart/form-data" id="<%= pageId %>" method="post">
        <p><a class="icon icon-arrow_switch" href="<%= wp.url("", "typeId", null, "id", null) %>">Change Content</a>
        <% wp.include("/WEB-INF/errors.jsp"); %>
        <% wp.include("/WEB-INF/objectForm.jsp", "object", object); %>
        <div class="buttons">
            <input type="submit" value="Save" />
        </div>
    </form>

    <script type="text/javascript">
        if (typeof jQuery !== 'undefined') (function($) {

            var $source = $('#<%= pageId %>').popup('source');
            if ($source.length > 0) {

                // Change the source link so that next click comes back here.
                var href = $source.attr('href');
                href = href.replace(/([?&])id=[^&]*/, '$1');
                href += '&id=<%= state.getId() %>';
                $source.attr('href', href);

                // Change the JSON definition object.
                var definition = $source.closest('.section').data('definition');
                definition.content = '<%= wp.js(state.getId()) %>';
                definition.contentTypeLabel = '<%= wp.js(state.getType().getLabel()) %>';
                definition.contentLabel = '<%= wp.js(state.getLabel()) %>';
                $source.text(definition.contentTypeLabel + ': ' + definition.contentLabel);
                $source.closest('.pageLayout-visual').trigger('updateJson');
            }
        })(jQuery);
    </script>
<% } %>
