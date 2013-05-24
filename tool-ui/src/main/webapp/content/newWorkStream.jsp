<%@ page import="

com.psddev.cms.db.WorkStream,
com.psddev.cms.tool.Search,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.Query,
com.psddev.dari.util.ObjectUtils,

java.util.Map
" %><%

ToolPageContext wp = new ToolPageContext(pageContext);

if (wp.requireUser()) {
    return;
}

WorkStream object = (WorkStream) wp.findOrReserve(WorkStream.class);

if (wp.isFormPost() &&
        wp.param(String.class, "action-save") != null) {
    try {
        wp.include("/WEB-INF/objectPost.jsp", "object", object);

        String searchString = wp.param(String.class, "search");

        if (!ObjectUtils.isBlank(searchString)) {
            Search search = new Search();
            search.getState().setValues((Map<String, Object>) ObjectUtils.fromJson(searchString));
            object.setSearch(search);

        } else {
            String queryString = wp.param(String.class, "query");
            Query<?> query = Query.fromAll();
            query.getState().setValues((Map<String, Object>) ObjectUtils.fromJson(queryString));
            object.setQuery(query);
        }

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
    wp.writeFormHeading(object);
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
            wp.writeStart("button",
                    "class", "action icon icon-action-save",
                    "name", "action-save",
                    "value", "true");
                wp.writeHtml("Save");
            wp.writeEnd();
        wp.writeEnd();
    wp.writeEnd();
wp.writeEnd();
%>
