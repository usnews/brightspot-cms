<%@ page session="false" import="

com.psddev.cms.db.Content,
com.psddev.cms.db.ToolUser,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.ObjectField,
com.psddev.dari.db.ObjectType,
com.psddev.dari.db.State,
com.psddev.dari.util.ObjectUtils,
com.psddev.dari.util.TypeReference,

java.util.Collection,
java.util.Date,
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

            Date oldUpdateDate = wp.param(Date.class, state.getId() + "/_updateDate");

            if (oldUpdateDate != null) {
                Content.ObjectModification contentData = state.as(Content.ObjectModification.class);
                Date newUpdateDate = contentData.getUpdateDate();

                if (!oldUpdateDate.equals(newUpdateDate)) {
                    ToolUser updateUser = contentData.getUpdateUser();

                    throw new IllegalArgumentException(
                            (updateUser != null ? updateUser.getLabel() : "Unknown user") +
                            " has updated this content at " +
                            newUpdateDate +
                            " since you've seen it last. Click on publish button again to override the changes with your own.");
                }
            }
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
