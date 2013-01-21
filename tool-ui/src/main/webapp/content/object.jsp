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
        wp.redirect("");
        return;
    } catch (Exception ex) {
        wp.getErrors().add(ex);
    }
}

// --- Presentation ---

%><% wp.include("/WEB-INF/objectHeading.jsp", "object", object); %>
<% wp.include("/WEB-INF/errors.jsp"); %>
<form action="<%= wp.objectUrl("", object) %>" enctype="multipart/form-data" method="post">
    <% wp.include("/WEB-INF/objectForm.jsp", "object", object); %>
    <div class="buttons">
        <input type="submit" name="action" value="Save" />
        <input class="delete text link" type="submit" name="action" value="Delete" onclick="return confirm('Are you sure you want to delete?');" />
    </div>
</form>
