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
java.util.HashSet,
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

wp.writeStart("div", "class", "objectInputs");
    String noteHtml = type.as(ToolUi.class).getEffectiveNoteHtml(object);

    if (!ObjectUtils.isBlank(noteHtml)) {
        wp.write("<div class=\"message message-info\">");
        wp.write(noteHtml);
        wp.write("</div>");
    }

    if (fields != null) {
        Object old = request.getAttribute("modificationHeadings");

        try {
            request.setAttribute("modificationHeadings", new HashSet<String>());

            for (ObjectField field : fields) {
                String name = field.getInternalName();

                if ((includeFields == null ||
                        includeFields.contains(name)) &&
                        (excludeFields == null ||
                        !excludeFields.contains(name))) {
                    wp.renderField(object, field);
                }
            }

        } finally {
            request.setAttribute("modificationHeadings", old);
        }

    } else { %>
        <div class="inputContainer">
            <div class="inputLabel"><label for="<%= wp.createId() %>">Data</label></div>
            <div class="inputSmall"><textarea cols="100" id="<%= wp.getId() %>" name="data" rows="20"><%= wp.h(state.getJsonString()) %></textarea></div>
        </div>
    <% }
wp.writeEnd();
%>
