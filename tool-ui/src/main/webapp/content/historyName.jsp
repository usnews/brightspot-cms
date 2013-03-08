<%@ page import="

com.psddev.cms.db.History,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.Query,

java.util.UUID
" %><%

ToolPageContext wp = new ToolPageContext(pageContext);

if (wp.requireUser()) {
    return;
}

History history = Query.findById(History.class, wp.param(UUID.class, "id"));

if (wp.isFormPost()) {
    try {
        history.setName(wp.param(String.class, "name"));
        history.save();

        wp.writeStart("script", "type", "text/javascript");
            wp.write("window.location = window.location;");
        wp.writeEnd();

        return;

    } catch (Exception ex) {
        wp.getErrors().add(ex);
    }
}

wp.include("/WEB-INF/header.jsp");

wp.writeStart("h1", "class", "icon icon-object-history");
    wp.writeHtml("Edit Revision Name");
wp.writeEnd();

wp.include("/WEB-INF/errors.jsp");

wp.writeStart("form", "method", "post", "action", wp.url(""));
    wp.writeStart("div", "class", "inputContainer");
        wp.writeStart("div", "class", "label");
            wp.writeStart("label", "for", wp.createId()).writeHtml("Name").writeEnd();
        wp.writeEnd();

        wp.writeStart("div", "class", "smallInput");
            wp.writeTag("input",
                    "type", "text",
                    "id", wp.getId(),
                    "name", "name",
                    "value", history.getName());
        wp.writeEnd();
    wp.writeEnd();

    wp.writeStart("div", "class", "buttons");
        wp.writeTag("input", "type", "submit", "value", "Save");
    wp.writeEnd();
wp.writeEnd();
%>
