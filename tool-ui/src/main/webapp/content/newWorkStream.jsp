<%@ page import="

com.psddev.cms.db.WorkStream,
com.psddev.cms.tool.Search,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.util.ObjectUtils,

java.util.Map
" %><%

ToolPageContext wp = new ToolPageContext(pageContext);

if (wp.requireUser()) {
    return;
}

WorkStream object = (WorkStream) wp.findOrReserve(WorkStream.class);

if (wp.isFormPost()) {
    try {
        wp.include("/WEB-INF/objectPost.jsp", "object", object);

        Search search = new Search();
        search.getState().setValues((Map<String, Object>) ObjectUtils.fromJson(wp.param(String.class, "search")));
        object.setSearch(search);
        object.setIncompleteIfMatching(wp.param(boolean.class, "incompleteIfMatching"));

        wp.publish(object);

        wp.writeStart("script", "type", "text/javascript");
            wp.write("window.location = window.location;");
        wp.writeEnd();

        return;

    } catch (Exception error) {
        wp.getErrors().add(error);
    }
}

wp.writeStart("div", "class", "widget");
    wp.include("/WEB-INF/objectHeading.jsp", "object", object);
    wp.include("/WEB-INF/errors.jsp");

    wp.writeStart("form",
            "method", "post",
            "action", wp.objectUrl("", object));
        wp.writeTag("input",
                "type", "hidden",
                "name", "incompleteIfMatching",
                wp.param(boolean.class, "incompleteIfMatching"));

        wp.include("/WEB-INF/objectForm.jsp", "object", object);

        wp.writeStart("div", "class", "buttons");
            wp.writeStart("button", "class", "action action-save");
                wp.writeHtml("Save");
            wp.writeEnd();
        wp.writeEnd();
    wp.writeEnd();
wp.writeEnd();
%>
