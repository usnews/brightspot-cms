<%@ page import="

com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.State
" %><%

ToolPageContext wp = new ToolPageContext(pageContext);
Object object = request.getAttribute("object");
State state = State.getInstance(object);

wp.writeFormHeading(object);

wp.writeStart("div", "class", "widgetControls");
    wp.include("/WEB-INF/objectVariation.jsp", "object", object);
wp.writeEnd();

wp.include("/WEB-INF/objectMessage.jsp");

wp.writeStart("form",
        "method", "post",
        "enctype", "multipart/form-data",
        "action", wp.objectUrl("", object),
        "autocomplete", "off");
    wp.include("/WEB-INF/objectForm.jsp");

    wp.writeStart("div", "class", "buttons");
        wp.writeStart("button",
                "class", "action action-save",
                "name", "action",
                "value", "Save");
            wp.writeHtml("Save");
        wp.writeEnd();

        if (state != null && !state.isNew()) {
            wp.writeStart("button",
                    "class", "action action-delete action-pullRight link",
                    "name", "action",
                    "value", "Delete");
                wp.writeHtml("Delete");
            wp.writeEnd();
        }
    wp.writeEnd();
wp.writeEnd();
%>
