<%@ page import="

com.psddev.cms.tool.CmsTool,
com.psddev.cms.tool.ToolPageContext,
com.psddev.cms.tool.Widget,

com.psddev.dari.db.State,
com.psddev.dari.util.ObjectUtils,
com.psddev.dari.util.StorageItem,

java.util.List
" %><%!

private static final String[] POSITIONS = new String[] {
        CmsTool.CONTENT_BOTTOM_WIDGET_POSITION,
        CmsTool.CONTENT_RIGHT_WIDGET_POSITION };
%><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);
if (wp.requirePermission("area/dashboard")) {
    return;
}

Object object = wp.findOrReserve();
if (wp.include("/WEB-INF/objectPublish.jsp", "object", object)) {
    return;
}

String pageId = wp.createId();
State state = State.getInstance(object);
StorageItem preview = state.getPreview();

// --- Presentation ---

wp.writeHeader();
wp.include("/WEB-INF/objectHeading.jsp", "object", object);
if (wp.hasPermission("type/" + state.getTypeId() + "/write")) {
    wp.write("<p style=\"position: absolute; right: 15px; top: 8px;\">");
    wp.write("<a class=\"action-edit\" href=\"");
    wp.write(wp.objectUrl("/content/edit.jsp", object));
    wp.write("\" target=\"_blank\">Edit in Full</a>");
    wp.write("</p>");
}
%>
<form action="<%= wp.objectUrl("", object) %>" enctype="multipart/form-data" id="<%= pageId %>" method="post">

    <% wp.include("/WEB-INF/errors.jsp"); %>
    <% wp.include("/WEB-INF/objectForm.jsp", "object", object); %>

    <div style="display: none;">
        <%
        for (String position : POSITIONS) {
            for (List<Widget> widgets : wp.getTool().findWidgets(position)) {
                for (Widget widget : widgets) {

                    wp.write("<input type=\"hidden\" name=\"");
                    wp.write(wp.h(state.getId()));
                    wp.write("/_widget\" value=\"");
                    wp.write(wp.h(widget.getId()));
                    wp.write("\">");

                    String display = widget.display(wp, object);
                    wp.write(display);
                }
            }
        }
        %>
    </div>
    <%    
        if (wp.hasPermission("type/" + state.getTypeId() + "/write")) {
            wp.write("<div class=\"buttons\">");
            wp.write("<input type=\"submit\" name=\"action\" value=\"Publish\" />");
            wp.write("</div>");
        }else{
            wp.write("<div class=\"warning message\"><p>You cannot edit this ");
            wp.write(wp.typeLabel(state));
            wp.write("!</p></div>");
        }
        
    %>
</form>

<script type="text/javascript">
if (typeof jQuery !== 'undefined') jQuery(function($) {
    var $page = $('#<%= pageId %>');

    <% if (!state.isNew()) { %>
        var $input = $page.popup('source').parent().find(':input.objectId');
        $input.attr('data-label', '<%= wp.js(state.getLabel()) %>');
        $input.attr('data-preview', '<%= wp.js(preview != null ? preview.getUrl() : "") %>');
        $input.val('<%= wp.js(state.getId()) %>');
        $input.change();
    <% } %>

    <% if (wp.isFormPost() && !wp.getErrors().isEmpty()) { %>
        $page.popup('restoreOriginalPosition');
    <% } %>

    <% if (wp.param("published") != null) { %>
        $page.popup('restoreOriginalPosition');
        $page.popup('close');
    <% } %>
});
</script>

<% wp.writeFooter(); %>
