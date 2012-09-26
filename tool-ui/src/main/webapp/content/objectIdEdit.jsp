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

%><p style="position: absolute; right: 15px; top: 8px;"><a class="icon-pencil" href="<%= wp.objectUrl("/content/edit.jsp", object) %>" target="_blank">Edit in Full</a></p>
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

    <div class="buttons">
        <input type="submit" name="action" value="Publish" />
    </div>
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

    <% if (wp.isFormPost() && wp.getErrors().size() == 0) { %>
        $page.popup('close');
    <% } %>
});
</script>

<% wp.writeFooter(); %>
