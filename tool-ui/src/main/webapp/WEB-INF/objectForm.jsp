<%@ page import="

com.psddev.cms.db.ToolUi,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.Modification,
com.psddev.dari.db.ObjectField,
com.psddev.dari.db.ObjectType,
com.psddev.dari.db.State,
com.psddev.dari.util.ObjectUtils,
com.psddev.dari.util.TypeReference,

java.util.ArrayList,
java.util.Collection,
java.util.Iterator,
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
        List<ObjectField> firsts = new ArrayList<ObjectField>();
        List<ObjectField> lasts = new ArrayList<ObjectField>();

        for (Iterator<ObjectField> i = fields.iterator(); i.hasNext(); ) {
            ObjectField field = i.next();
            ToolUi ui = field.as(ToolUi.class);

            if (ui.isDisplayFirst()) {
                firsts.add(field);
                i.remove();

            } else if (ui.isDisplayLast()) {
                lasts.add(field);
                i.remove();
            }
        }

        fields.addAll(0, firsts);
        fields.addAll(lasts);

        Object old = request.getAttribute("modificationHeadings");

        try {
            request.setAttribute("modificationHeadings", new HashSet<String>());

            if (request.getAttribute("firstDraft") == null) {
                request.setAttribute("firstDraft", state.isNew());
                request.setAttribute("finalDraft", state.isNew() || state.isVisible());
            }

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
            request.setAttribute("firstDraft", null);
            request.setAttribute("finalDraft", null);
        }

    } else { %>
        <div class="inputContainer">
            <div class="inputLabel"><label for="<%= wp.createId() %>">Data</label></div>
            <div class="inputSmall"><textarea cols="100" id="<%= wp.getId() %>" name="data" rows="20"><%= wp.h(state.getJsonString()) %></textarea></div>
        </div>
    <% }
wp.writeEnd();
%>
