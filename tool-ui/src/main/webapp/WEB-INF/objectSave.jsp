<%@ page import="

com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.CachingDatabase,
com.psddev.dari.db.Query,
com.psddev.dari.db.State,

java.util.UUID
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);
if (!wp.isFormPost() || !"Save".equals(wp.param("action"))) {
    return;
}

Object object = request.getAttribute("object");
State state = State.getInstance(object);
try {

    wp.include("/WEB-INF/objectPost.jsp");

    UUID variationId = wp.uuidParam("variationId");
    if (variationId != null) {
        Object original = Query.
                from(Object.class).
                where("_id = ?", state.getId()).
                option(CachingDatabase.IS_DISABLED_QUERY_OPTION, Boolean.TRUE).
                first();
        State.getInstance(original).putValue("variations/" + variationId.toString(), state.getSimpleFieldedValues());
        object = original;
        state = State.getInstance(object);
    }

    wp.publish(object);
    wp.redirect("", "id", state.getId(), "saved", System.currentTimeMillis());

} catch (Exception ex) {
    wp.getErrors().add(ex);
}
%>
