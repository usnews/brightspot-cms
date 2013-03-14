<%@ page import="

com.psddev.cms.db.WorkStream,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.util.ObjectUtils
" %><%

ToolPageContext wp = new ToolPageContext(pageContext);

if (wp.requireUser()) {
    return;
}

WorkStream object = (WorkStream) wp.findOrReserve();

if (wp.isFormPost()) {
    try {
        if (wp.param(String.class, "action-save") != null) {
            wp.include("/WEB-INF/objectPost.jsp", "object", object);
            wp.publish(object);

        } else if (wp.param(String.class, "action-delete") != null) {
            wp.deleteSoftly(object);
        }

        wp.writeStart("script", "type", "text/javascript");
            wp.write("top.window.location = top.window.location;");
        wp.writeEnd();

        return;

    } catch (Exception ex) {
        wp.getErrors().add(ex);
    }
}

wp.writeFormHeading(object, "class", "icon icon-object-workStream");

wp.writeStart("p");
    wp.writeStart("a",
            "class", "icon icon-action-search",
            "href", wp.cmsUrl("/misc/savedSearch.jsp", "search", ObjectUtils.toJson(object.getSearch().getState().getSimpleValues())),
            "target", "miscSavedSearch");
        wp.writeHtml("View Items");
    wp.writeEnd();
wp.writeEnd();

wp.include("/WEB-INF/errors.jsp");

wp.writeStart("form",
        "method", "post",
        "enctype", "multipart/form-data",
        "action", wp.objectUrl("", object));
    wp.include("/WEB-INF/objectForm.jsp", "object", object);

    wp.writeStart("div", "class", "buttons");
        wp.writeStart("button",
                "class", "action action-save",
                "name", "action-save",
                "value", true);
            wp.writeHtml("Save");
        wp.writeEnd();

        wp.writeStart("button",
                "class", "action action-delete action-pullRight link",
                "name", "action-delete",
                "value", true,
                "onclick", "return confirm('Are you sure you want to delete?');");
            wp.writeHtml("Delete");
        wp.writeEnd();
    wp.writeEnd();
wp.writeEnd();
%>
