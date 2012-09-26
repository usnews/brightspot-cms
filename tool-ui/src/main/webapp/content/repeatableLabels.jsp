<%@ page import="

com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.Recordable,
com.psddev.dari.db.ObjectType,
com.psddev.dari.db.State,

com.psddev.dari.util.ObjectUtils,

java.util.HashMap,
java.util.Map,
java.util.UUID
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);
Map<String, String> labels = new HashMap<String, String>();

UUID[] ids = wp.uuidParams("id");
UUID[] typeIds = wp.uuidParams("typeId");
for (int i = 0, s = Math.min(ids.length, typeIds.length); i < s; ++ i) {
    ObjectType type = ObjectType.getInstance(typeIds[i]);
    if (type != null) {
        Object object = type.createObject(ids[i]);
        State state = State.getInstance(object);
        try {
            state.beginWrites();
            wp.include("/WEB-INF/objectPost.jsp", "object", object);
        } finally {
            state.endWrites();
        }
        labels.put(state.getId().toString(), state.getLabel());
    }
}

// --- Presentation ---

response.setContentType("application/json");
wp.write(ObjectUtils.toJson(labels));
%>
