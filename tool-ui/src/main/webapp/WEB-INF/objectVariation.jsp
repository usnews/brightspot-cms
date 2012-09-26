<%@ page import="

com.psddev.cms.db.Variation,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.Query,
com.psddev.dari.db.State
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);
Object object = request.getAttribute("object");
State state = State.getInstance(object);
if (state.isNew()) {
    return;
}

if (Query.from(Variation.class).count() == 0) {
    return;
}

Variation selected = Query.findById(Variation.class, wp.uuidParam("variationId"));

// --- Presentation ---

%><div class="variation">
    <a href="<%= wp.returnableUrl("/content/variations.jsp", "variationId", selected != null ? selected.getId() : null) %>" target="objectVariation-<%= state.getId() %>">
        Variation: <%= selected != null ? wp.objectLabel(selected) : "Default" %>
    </a>
</div>
