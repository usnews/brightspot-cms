<%@ page import="

com.psddev.cms.db.ToolUser,
com.psddev.cms.tool.ToolPageContext,

java.util.Arrays,
java.util.Collection
" %><%!

private static final Collection<String> INCLUDE_FIELDS = Arrays.asList("name", "email", "password", "timeZone");
%><%

ToolPageContext wp = new ToolPageContext(pageContext);

if (wp.requireUser()) {
    return;
}

ToolUser user = wp.getUser();

if (wp.isFormPost()) {
    try {
        wp.include("/WEB-INF/objectPost.jsp", "object", user, "includeFields", INCLUDE_FIELDS);
        user.save();
        wp.redirect("");
        return;

    } catch (Exception ex) {
        wp.getErrors().add(ex);
    }
}

wp.writeStart("div", "class", "widget");
    wp.writeStart("h1", "class", "icon icon-object-toolUser").writeHtml("Profile").writeEnd();

    wp.include("/WEB-INF/errors.jsp");

    wp.writeStart("form",
            "method", "post",
            "enctype", "multipart/form-data",
            "action", wp.objectUrl("", user));
        wp.include("/WEB-INF/objectForm.jsp", "object", user, "includeFields", INCLUDE_FIELDS);

        wp.writeStart("div", "class", "actions");
            wp.writeStart("button", "class", "icon icon-action-save");
                wp.writeHtml("Save");
            wp.writeEnd();
        wp.writeEnd();
    wp.writeEnd();
wp.writeEnd();
%>
