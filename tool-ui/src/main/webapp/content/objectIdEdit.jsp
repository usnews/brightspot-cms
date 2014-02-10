<%@ page session="false" import="

com.psddev.cms.tool.CmsTool,
com.psddev.cms.tool.ToolPageContext,
com.psddev.cms.tool.Widget,

com.psddev.dari.db.State,
com.psddev.dari.util.HtmlWriter,
com.psddev.dari.util.ObjectUtils,
com.psddev.dari.util.StorageItem,

java.io.StringWriter,
java.util.List,
java.util.UUID
" %><%!

private static final String[] POSITIONS = new String[] {
        CmsTool.CONTENT_BOTTOM_WIDGET_POSITION,
        CmsTool.CONTENT_RIGHT_WIDGET_POSITION };
%><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);
if (wp.requireUser()) {
    return;
}

Object object = wp.findOrReserve();
if (wp.tryPublish(object)) {
    return;
}

String pageId = wp.createId();
State state = State.getInstance(object);
StorageItem preview = state.getPreview();

// --- Presentation ---

wp.writeHeader();
wp.writeFormHeading(object);
if (wp.hasPermission("type/" + state.getTypeId() + "/write")) {
    wp.writeStart("div", "class", "widgetControls");
        wp.write("<a class=\"action-edit\" href=\"");
        wp.write(wp.objectUrl("/content/edit.jsp", object, "variationId", wp.param(UUID.class, "variationId")));
        wp.write("\" target=\"_blank\">Edit in Full</a>");
        wp.write("</p>");
    wp.writeEnd();
}
%>
<form action="<%= wp.objectUrl("", object) %>" enctype="multipart/form-data" id="<%= pageId %>" method="post">

    <% wp.include("/WEB-INF/errors.jsp"); %>
    <% wp.writeFormFields(object); %>

    <div style="display: none;">
        <%
        for (String position : POSITIONS) {
            for (List<Widget> widgets : wp.getTool().findWidgets(position)) {
                for (Widget widget : widgets) {

                    wp.write("<input type=\"hidden\" name=\"");
                    wp.write(wp.h(state.getId()));
                    wp.write("/_widget\" value=\"");
                    wp.write(wp.h(widget.getInternalName()));
                    wp.write("\">");

                    String displayHtml;

                    try {
                        displayHtml = widget.createDisplayHtml(wp, object);

                    } catch (Exception ex) {
                        StringWriter sw = new StringWriter();
                        HtmlWriter hw = new HtmlWriter(sw);
                        hw.putAllStandardDefaults();
                        hw.start("pre", "class", "message message-error").object(ex).end();
                        displayHtml = sw.toString();
                    }

                    if (!ObjectUtils.isBlank(displayHtml)) {
                        wp.write(displayHtml);
                    }
                }
            }
        }
        %>
    </div>

    <%    
    if (wp.hasPermission("type/" + state.getTypeId() + "/write")) {
        wp.writeStart("div", "class", "buttons");
            if (wp.getUser().getCurrentSchedule() != null) {
                wp.writeStart("button",
                        "class", "icon icon-action-schedule",
                        "name", "action-publish",
                        "value", "true");
                    wp.writeHtml("Schedule");
                wp.writeEnd();

            } else {
                wp.writeStart("button",
                        "class", "icon icon-action-publish",
                        "name", "action-publish",
                        "value", "true");
                    wp.writeHtml("Publish");
                wp.writeEnd();
            }
        wp.writeEnd();

    } else {
        wp.writeStart("div", "class", "message message-warning");
            wp.writeHtml("You don't have permission to edit this content!");
        wp.writeEnd();
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
