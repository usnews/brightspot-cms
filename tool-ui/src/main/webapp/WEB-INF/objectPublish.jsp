<%@ page import="

com.psddev.cms.db.Content,
com.psddev.cms.db.Draft,
com.psddev.cms.db.Schedule,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.CachingDatabase,
com.psddev.dari.db.Query,
com.psddev.dari.db.State,

java.util.Date,
java.util.UUID
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);
if (!wp.isFormPost()) {
    return;
}

String action = wp.param("action");
if (!("Publish".equals(action)
        || "Update".equals(action)
        || "Schedule".equals(action)
        || "Reschedule".equals(action))) {
    return;
}

Object object = request.getAttribute("object");
State state = State.getInstance(object);
Draft draft = wp.getOverlaidDraft(object);
try {

    state.beginWrites();
    wp.include("/WEB-INF/objectPost.jsp", "object", object);
    wp.include("/WEB-INF/widgetsUpdate.jsp", "object", object);

    UUID variationId = wp.uuidParam("variationId");
    if (variationId != null) {
        Object original = Query.
                from(Object.class).
                where("_id = ?", state.getId()).
                option(CachingDatabase.IS_DISABLED_QUERY_OPTION, Boolean.TRUE).
                first();
        State.getInstance(original).putValue(
                "variations/" + variationId.toString(),
                state.getSimpleFieldedValues());
        object = original;
        state = State.getInstance(object);
    }

    Date publishDate = wp.dateParam("publishDate");
    if (publishDate != null && publishDate.before(new Date())) {
        state.as(Content.ObjectModification.class).setPublishDate(publishDate);
        publishDate = null;
    }

    if (publishDate != null) {

        if (draft == null) {
            draft = new Draft();
            draft.setOwner(wp.getUser());
            draft.setObject(object);
        } else {
            draft.setObject(object);
        }

        Schedule schedule = draft.getSchedule();
        if (schedule == null) {
            schedule = new Schedule();
            schedule.setTriggerSite(wp.getSite());
            schedule.setTriggerUser(wp.getUser());
        }
        schedule.setTriggerDate(publishDate);
        schedule.save();

        draft.setSchedule(schedule);
        draft.save();
        state.commitWrites();
        wp.redirect("", ToolPageContext.DRAFT_ID_PARAMETER, draft.getId());

    } else {
        if (draft != null) {
            draft.delete();
        }
        wp.publish(object);
        state.commitWrites();
        wp.redirect("",
                "_isFrame", wp.boolParam("_isFrame"),
                "id", state.getId(),
                "historyId", null,
                "copyId", null,
                "published", System.currentTimeMillis());
    }

} catch (Exception ex) {
    wp.getErrors().add(ex);

} finally {
    state.endWrites();
}
%>
