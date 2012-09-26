<%@ page import="

com.psddev.cms.db.Content,
com.psddev.cms.db.Draft,
com.psddev.cms.db.Schedule,
com.psddev.cms.tool.Widget,
com.psddev.cms.tool.JspWidget,

com.psddev.dari.db.Query,
com.psddev.dari.db.State,

com.psddev.cms.tool.ToolPageContext,

java.util.ArrayList,
java.util.List
" %><%

ToolPageContext wp = new ToolPageContext(pageContext);
if (JspWidget.isUpdating(wp)) {
    return;
}

Widget widget = JspWidget.getWidget(wp);
Object object = JspWidget.getObject(wp);
State state = State.getInstance(object);

List<Draft> drafts = new ArrayList<Draft>();
Draft overlaidDraft = wp.getOverlaidDraft(object);
Draft selected = null;
for (Draft draft : wp.getDatabase().readList(Query
        .from(Draft.class)
        .where("objectId = ?", state.getId()))) {
    if (draft.getSchedule() == null) {
        drafts.add(draft);
        if (draft.equals(overlaidDraft)) {
            selected = draft;
        }
    }
}

if (drafts.isEmpty()) {
    return;
}

%><ul class="links">
    <% if (!state.isNew()) { %>
        <li<%= overlaidDraft == null && selected == null
                ? " class=\"selected\"" : "" %>><a href="<%=
                wp.originalUrl(null, object)
                %>" target="_top">Original</a></li>
    <% } %>
    <% for (Draft draft : drafts) { %>
        <li<%= draft.equals(selected) ? " class=\"selected\"" : ""
                %>><a href="<%= wp.objectUrl(null, draft)
                %>" target="_top"><%= wp.h(draft.getName(), "Saved ")
                %><%= draft.as(Content.ObjectModification.class).getUpdateDate() %></a></li>
    <% } %>
</ul>
