<%@ page import="

com.psddev.cms.db.Draft,
com.psddev.cms.db.DraftStatus,
com.psddev.cms.db.Workflow,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.Query,
com.psddev.dari.db.State
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);
if (!wp.isFormPost()) {
    return;
}

String action = wp.param("action");
boolean isDraft = "Save".equals(action) || "Save Draft".equals(action);
DraftStatus status = null;
if (!isDraft) {
    Workflow workflow = Query.from(Workflow.class).where("name = ?", action).first();
    if (workflow != null) {
        isDraft = true;
        status = workflow.getTarget();
    }
}

if (!isDraft) {
    return;
}

Object object = request.getAttribute("object");
State state = State.getInstance(object);
Draft draft = wp.getOverlaidDraft(object);
try {

    state.beginWrites();
    wp.include("/WEB-INF/objectPost.jsp", "object", object);
    wp.updateUsingAllWidgets(object);

    if (draft == null || draft.getSchedule() != null) {
        draft = new Draft();
        draft.setOwner(wp.getUser());
        draft.setObject(object);
    } else {
        draft.setObject(object);
    }

    draft.setStatus(status);
    wp.publish(draft);
    state.commitWrites();
    wp.redirect("", ToolPageContext.DRAFT_ID_PARAMETER, draft.getId());

} catch (Exception ex) {
    wp.getErrors().add(ex);

} finally {
    state.endWrites();
}
%>
