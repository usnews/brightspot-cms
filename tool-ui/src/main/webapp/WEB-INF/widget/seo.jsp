<%@ page import="

com.psddev.cms.db.Page,
com.psddev.cms.db.Seo,
com.psddev.cms.db.Template,
com.psddev.cms.db.ToolUi,
com.psddev.cms.tool.Widget,
com.psddev.cms.tool.JspWidget,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.DatabaseEnvironment,
com.psddev.dari.db.ObjectField,
com.psddev.dari.db.ObjectType,
com.psddev.dari.db.State,
com.psddev.dari.util.ObjectUtils,
com.psddev.dari.util.StringUtils,

java.util.ArrayList,
java.util.Collection
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);
Widget widget = JspWidget.getWidget(wp);
Object object = JspWidget.getObject(wp);
State state = State.getInstance(object);
if (!Page.class.isInstance(object) &&
        Template.class.isInstance(object) &&
        !Template.Static.findUsedTypes(wp.getSite()).contains(state.getType())) {
    return;
}

DatabaseEnvironment environment = state.getDatabase().getEnvironment();
ObjectField titleField = environment.getField("cms.seo.title");
ObjectField descriptionField = environment.getField("cms.seo.description");
ObjectField keywordsField = environment.getField("cms.seo.keywords");

if (JspWidget.isUpdating(wp)) {
    wp.processField(object, titleField);
    wp.processField(object, descriptionField);
    wp.processField(object, keywordsField);
    return;
}

ObjectType type = state.getType();
if (type != null) {

    titleField = new ObjectField(titleField);
    putNote(type, titleField, type.getLabelFields(), "or", "Leave blank to use %s.");

    Seo.TypeModification typeSeo = type.as(Seo.TypeModification.class);
    descriptionField = new ObjectField(descriptionField);
    putNote(type, descriptionField, typeSeo.getDescriptionFields(), "or", "Leave blank to use %s.");

    keywordsField = new ObjectField(keywordsField);
    putNote(type, keywordsField, typeSeo.getKeywordsFields(), "and", "Additional to %s.");
}

// --- Presentation ---

for (ObjectField field : new ObjectField[] {
        titleField, descriptionField, keywordsField }) {
    wp.renderField(object, field);
}
%><%!

private static void putNote(ObjectType type, ObjectField field, Collection<String> noteFieldNames, String joinWord, String note) {

    if (ObjectUtils.isBlank(noteFieldNames)) {
        return;
    }

    Collection<ObjectField> noteFields = new ArrayList<ObjectField>();
    for (String name : noteFieldNames) {
        ObjectField noteField = type.getField(name);
        if (noteField != null) {
            noteFields.add(noteField);
        }
    }

    if (noteFields.size() == 0) {
        return;
    }

    StringBuilder sb = new StringBuilder();

    if (noteFieldNames.size() == 1) {
        for (ObjectField noteField : noteFields) {
            sb.append("the ");
            sb.append(noteField.getLabel());
        }

    } else if (noteFieldNames.size() == 2) {
        for (ObjectField noteField : noteFields) {
            sb.append(noteField.getLabel());
            sb.append(" ");
            sb.append(joinWord);
            sb.append(" ");
        }
        sb.setLength(sb.length() - joinWord.length() - 2);

    } else {
        int index = 0;
        int last = noteFieldNames.size();
        for (ObjectField noteField : noteFields) {
            ++ index;
            if (index < last) {
                sb.append(noteField.getLabel());
                sb.append(", ");
            } else {
                sb.append(" ");
                sb.append(joinWord);
                sb.append(" ");
                sb.append(noteField.getLabel());
            }
        }
    }

    field.as(ToolUi.class).setNoteHtml(StringUtils.escapeHtml(String.format(note, sb)));
}
%>
