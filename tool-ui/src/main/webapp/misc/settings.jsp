<%@ page import="

com.psddev.cms.db.ToolUser,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.util.Password,

java.util.Arrays,
java.util.Collection
" %><%!

private static final Collection<String> INCLUDE_FIELDS = Arrays.asList("name", "email", "password");
%><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);

if (wp.requireUser()) {
    return;
}

ToolUser user = wp.getUser();
if (wp.isFormPost()) {
    try {
        wp.include("/WEB-INF/objectPost.jsp", "object", user, "includeFields", INCLUDE_FIELDS);
        wp.publish(user);
        wp.redirect("");
        return;
    } catch (Exception ex) {
        wp.getErrors().add(ex);
    }
}

// --- Presentation ---

%><h1 class="icon icon-object-toolUser">Profile</h1>
<% wp.include("/WEB-INF/errors.jsp"); %>
<form action="<%= wp.objectUrl("", user) %>" enctype="multipart/form-data" method="post">
    <% wp.include("/WEB-INF/objectForm.jsp", "object", user, "includeFields", INCLUDE_FIELDS); %>
    <div class="buttons">
        <input type="submit" name="action" value="Save" />
    </div>
</form>
