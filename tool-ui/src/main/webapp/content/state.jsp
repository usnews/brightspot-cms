<%@ page import="

com.psddev.cms.db.ContentSection,
com.psddev.cms.db.Page,
com.psddev.cms.db.Section,
com.psddev.cms.db.Template,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.Query,
com.psddev.dari.db.ObjectType,
com.psddev.dari.db.State,

com.psddev.dari.util.ObjectUtils,

java.util.Map,
java.util.Set,
java.util.UUID
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);
Object selected = wp.findOrReserve();
State state = State.getInstance(selected);

Template template = null;
if (selected != null) {
    template = state.as(Template.ObjectModification.class).getDefault();
}
if (template == null) {
    template = Query.findById(
            Template.class, wp.uuidParam("templateId"));
    if (template != null) {
        Set<ObjectType> types = template.getContentTypes();
        if (types != null && types.size() == 1) {
            for (ObjectType type : types) {
                selected = wp.findOrReserve(type.getId());
                state = State.getInstance(selected);
            }
        }
    }
    if (selected != null) {
        state.as(Template.ObjectModification.class).setDefault(template);
    }
}

if (selected == null) {
    return;
}

Object editing = selected;
Section selectedSection = null;
if (selected instanceof Page) {
    UUID sectionId = wp.uuidParam("sectionId");
    if (!state.getId().equals(sectionId)) {
        selectedSection = Query.findById(Section.class, wp.uuidParam("sectionId"));
        if (selectedSection instanceof ContentSection) {
            editing = ((ContentSection) selectedSection).getContent();
        }
    }
}

State editingState = State.getInstance(editing);
try {
    editingState.beginWrites();
    wp.include("/WEB-INF/objectPost.jsp", "object", editing);
    wp.include("/WEB-INF/widgetsUpdate.jsp", "object", editing);
    wp.publish(editing);
} catch (Exception error) {
} finally {
    editingState.endWrites();
}

// --- Presentation ---

response.setContentType("application/json");
Map<String, Object> data = (Map<String, Object>) ObjectUtils.fromJson(editingState.getJsonString());
data.put("_id", editingState.getId().toString());
data.put("_typeId", editingState.getTypeId().toString());
wp.write(ObjectUtils.toJson(data));
%>
