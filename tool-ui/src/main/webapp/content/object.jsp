<%@ page import="

com.psddev.cms.tool.ToolPageContext
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);
if (wp.requireUser()) {
    return;
}

Object object = wp.findOrReserve();
if (wp.isFormPost()) {
    try {
        String action = wp.param("action");
        if ("Save".equals(action)) {
            wp.include("/WEB-INF/objectPost.jsp", "object", object);
            wp.publish(object);
        } else if ("Delete".equals(action)) {
            wp.deleteSoftly(object);
        }
        if (wp.param(boolean.class, "reload")) {
            wp.writeStart("script", "type", "text/javascript");
                wp.write("top.window.location = top.window.location;");
            wp.writeEnd();
        } else {
            wp.redirect("");
        }
        return;
    } catch (Exception ex) {
        wp.getErrors().add(ex);
    }
}

// --- Presentation ---

%><% wp.include("/WEB-INF/objectHeading.jsp", "object", object); %>
<% wp.include("/WEB-INF/errors.jsp"); %>
<form action="<%= wp.objectUrl("", object) %>" enctype="multipart/form-data" method="post">
    <input type="hidden" name="reload" value="<%= wp.param(boolean.class, "reload") %>">
    <% wp.include("/WEB-INF/objectForm.jsp", "object", object); %>
    <div class="buttons">
        <button class="action action-save" name="action" value="Save">Save</button>
        <button class="action action-delete action-pullRight link" name="action" value="Delete" onclick="return confirm('Are you sure you want to delete?');">Delete</button>
    </div>
</form>
