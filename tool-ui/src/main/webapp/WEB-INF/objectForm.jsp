<%@ page import="

com.psddev.cms.db.ToolUi,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.Modification,
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

// --- Presentation ---

String noteHtml = type.as(ToolUi.class).getEffectiveNoteHtml(object);
if (!ObjectUtils.isBlank(noteHtml)) {
    wp.write("<div class=\"message info\">");
    wp.write(noteHtml);
    wp.write("</div>");
}

if (fields != null) {
    String lastDeclaring = null;

    for (ObjectField field : fields) {
        String name = field.getInternalName();

        String heading = field.as(ToolUi.class).getHeading();
        if (!ObjectUtils.isBlank(heading)) {
            wp.write("<h2 style=\"margin-top: 20px;\">");
            wp.write(wp.h(heading));
            wp.write("</h2>");
        }

        if ((includeFields == null ||
                includeFields.contains(name)) &&
                (excludeFields == null ||
                !excludeFields.contains(name))) {

            String declaring = field.getJavaDeclaringClassName();
            if (lastDeclaring != null && !lastDeclaring.equals(declaring)) {
                ObjectType declaringType = ObjectType.getInstance(declaring);
                if (declaringType != null &&
                        declaringType.getGroups().contains(Modification.class.getName()) &&
                        !declaringType.as(ToolUi.class).isHidden()) {
                    wp.write("<h2 style=\"margin-top: 20px;\">");
                    wp.write(wp.objectLabel(declaringType));
                    wp.write("</h2>");
                }
            }

            lastDeclaring = declaring;
            wp.renderField(object, field);
        }
    }

} else { %>
    <div class="inputContainer">
        <div class="label"><label for="<%= wp.createId() %>">Data</label></div>
        <div class="smallInput"><textarea cols="100" id="<%= wp.getId() %>" name="data" rows="20"><%= wp.h(state.getJsonString()) %></textarea></div>
    </div>
<% } %>
