<%@ page import="

com.psddev.cms.db.Draft,
com.psddev.cms.db.Trash,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.State
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);
if (!wp.isFormPost() || !"Delete".equals(wp.param("action"))) {
    return;
}

Object object = request.getAttribute("object");
try {

    Draft draft = wp.getOverlaidDraft(object);
    if (draft != null) {
        draft.delete();
        wp.redirect("", "discarded", System.currentTimeMillis());

    } else {
        Trash trash = wp.deleteSoftly(object);
        wp.redirect("", "id", null, "saved", null);
    }

} catch (Exception ex) {
    wp.getErrors().add(ex);
}
%>
