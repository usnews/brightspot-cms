<%@ page session="false" import="

com.psddev.cms.db.Content,
com.psddev.cms.db.Draft,
com.psddev.cms.db.History,
com.psddev.cms.db.Schedule,
com.psddev.cms.db.Template,
com.psddev.cms.db.Trash,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.Query,
com.psddev.dari.db.State,

java.util.Date,
java.util.List,

org.joda.time.DateTime
" %><%

// --- Presentation ---

ToolPageContext wp = new ToolPageContext(pageContext);
Object object = request.getAttribute("object");

List<Throwable> errors = wp.getErrors();
if (errors != null && errors.size() > 0) {
    wp.include("/WEB-INF/errors.jsp");
    return;
}

Trash deleted = Query.findById(Trash.class, wp.uuidParam("deleted"));
if (deleted != null) {
    wp.write("<div class=\"message message-warning\"><p>");
    wp.write("Deleted ", deleted.getDeleteDate());
    wp.write(" by ", wp.objectLabel(deleted.getDeleteUser()));
    wp.write(".<p></div>");
    return;
}

State state = State.getInstance(object);
Content.ObjectModification contentData = state.as(Content.ObjectModification.class);

if (wp.getUser().equals(contentData.getUpdateUser())) {
    Date updateDate = contentData.getUpdateDate();

    if (updateDate != null && updateDate.after(new DateTime().minusSeconds(10).toDate())) {
        wp.write("<div class=\"message message-success\"><p>");
        wp.write("Published ");
        wp.writeHtml(wp.formatUserDateTime(updateDate));//
        wp.write(".</p>");
        wp.write("</div>");
        return;
    }
}

Date saved = wp.dateParam("saved");
if (saved != null) {
    wp.write("<div class=\"message message-success\"><p>");
    wp.write("Saved ", saved);
    wp.write(".</p></div>");
    return;
}
%>
