<%@ page session="false" import="

com.psddev.cms.db.Content,
com.psddev.cms.db.ContentField,
com.psddev.cms.db.ContentType,
com.psddev.cms.db.ToolUi,
com.psddev.cms.db.Workflow,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.Modification,
com.psddev.dari.db.ObjectField,
com.psddev.dari.db.ObjectType,
com.psddev.dari.db.Query,
com.psddev.dari.db.State,
com.psddev.dari.util.ObjectUtils,
com.psddev.dari.util.TypeReference,

java.util.ArrayList,
java.util.Collection,
java.util.Date,
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

Object oldContainer = request.getAttribute("containerObject");

try {
    if (oldContainer == null) {
        request.setAttribute("containerObject", object);
    }

    wp.writeStart("div",
            "class", "objectInputs",
            "data-type", type != null ? type.getInternalName() : null,
            "data-id", state.getId(),
            "data-object-id", state.getId(),
            "data-widths", "{ \"objectInputs-small\": { \"<=\": 350 } }");

        Object original = Query.fromAll().where("_id = ?", state.getId()).master().noCache().first();

        if (original != null) {
            Date updateDate = State.getInstance(original).as(Content.ObjectModification.class).getUpdateDate();

            if (updateDate != null) {
                wp.writeElement("input",
                        "type", "hidden",
                        "name", state.getId() + "/_updateDate",
                        "value", updateDate.getTime());
            }
        }

        if (type != null) {
            String noteHtml = type.as(ToolUi.class).getEffectiveNoteHtml(object);

            if (!ObjectUtils.isBlank(noteHtml)) {
                wp.write("<div class=\"message message-info\">");
                wp.write(noteHtml);
                wp.write("</div>");
            }
        }

        if (object instanceof Query) {
            wp.writeStart("div", "class", "queryField");
                wp.writeElement("input",
                        "type", "text",
                        "name", state.getId() + "/_query",
                        "value", ObjectUtils.toJson(state.getSimpleValues()));
            wp.writeEnd();

        } else if (fields != null) {
            ContentType ct = type != null ? Query.from(ContentType.class).where("internalName = ?", type.getInternalName()).first() : null;

            if (ct != null) {
                List<ObjectField> firsts = new ArrayList<ObjectField>();

                for (ContentField cf : ct.getFields()) {
                    for (Iterator<ObjectField> i = fields.iterator(); i.hasNext(); ) {
                        ObjectField field = i.next();

                        if (field.getInternalName().equals(cf.getInternalName())) {
                            firsts.add(field);
                            i.remove();
                            break;
                        }
                    }
                }

                fields.addAll(0, firsts);

            } else {
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
            }

            boolean draftCheck = false;

            try {
                if (request.getAttribute("firstDraft") == null) {
                    draftCheck = true;
                    request.setAttribute("firstDraft", state.isNew());
                    request.setAttribute("finalDraft", !state.isNew() &&
                            !state.as(Content.ObjectModification.class).isDraft() &&
                            state.as(Workflow.Data.class).getCurrentState() == null &&
                            wp.getOverlaidDraft(object) == null);
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
                if (draftCheck) {
                    request.setAttribute("firstDraft", null);
                    request.setAttribute("finalDraft", null);
                }
            }

        } else {
            wp.writeStart("div", "class", "inputContainer");
                wp.writeStart("div", "class", "inputLabel");
                    wp.writeStart("label", "for", wp.createId());
                        wp.writeHtml("Data");
                    wp.writeEnd();
                wp.writeEnd();

                wp.writeStart("div", "class", "inputSmall");
                    wp.writeStart("textarea",
                            "data-code-type", "text/json",
                            "id", wp.getId(),
                            "name", "data");
                        wp.writeHtml(ObjectUtils.toJson(state.getSimpleValues(), true));
                    wp.writeEnd();
                wp.writeEnd();
            wp.writeEnd();
        }
    wp.writeEnd();

} finally {
    if (oldContainer == null) {
        request.setAttribute("containerObject", null);
    }
}
%>
