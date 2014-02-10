<%@ page session="false" import="

com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.ObjectField,
com.psddev.dari.db.ObjectType,
com.psddev.dari.db.State,
com.psddev.dari.util.ObjectUtils,
com.psddev.dari.util.TypeReference,

java.util.Collection,
java.util.List
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);
Object object = request.getAttribute("object");
Collection<String> includeFields = ObjectUtils.to(new TypeReference<Collection<String>>() { }, request.getAttribute("includeFields"));
Collection<String> excludeFields = ObjectUtils.to(new TypeReference<Collection<String>>() { }, request.getAttribute("excludeFields"));
State state = State.getInstance(object);
ObjectType type = state.getType();
List<ObjectField> fields = type != null ? type.getFields() : null;

if (fields != null) {
    Object oldContainer = request.getAttribute("containerObject");
    boolean draftCheck = false;

    try {
        if (oldContainer == null) {
            request.setAttribute("containerObject", object);
        }

        if (request.getAttribute("firstDraft") == null) {
            draftCheck = true;
            request.setAttribute("firstDraft", state.isNew());
            request.setAttribute("finalDraft", state.isNew() || state.isVisible());
        }

        for (ObjectField field : fields) {
            String name = field.getInternalName();

            if ((includeFields == null ||
                    includeFields.contains(name)) &&
                    (excludeFields == null ||
                    !excludeFields.contains(name))) {
                wp.processField(object, field);
            }
        }

    } finally {
        if (oldContainer == null) {
            request.setAttribute("containerObject", null);
        }

        if (draftCheck) {
            request.setAttribute("firstDraft", null);
            request.setAttribute("finalDraft", null);
        }
    }

} else {
    state.setJsonString(wp.param("data", ""));
}
%>
