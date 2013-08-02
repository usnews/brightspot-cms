<%@ page import="

com.psddev.cms.db.ReferentialTextMarker,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.ObjectField,
com.psddev.dari.db.ObjectType,
com.psddev.dari.db.Reference,
com.psddev.dari.db.ReferentialText,
com.psddev.dari.db.Query,
com.psddev.dari.db.State,
com.psddev.dari.util.StorageItem,

com.psddev.dari.util.ObjectUtils,
com.psddev.dari.util.StringUtils,

java.util.Map,
java.util.UUID,
java.util.regex.Matcher
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);
Object object = request.getAttribute("object");
State state = State.getInstance(object);

ObjectField field = (ObjectField) request.getAttribute("field");
String fieldName = field.getInternalName();
ReferentialText fieldValue = (ReferentialText) state.getValue(fieldName);

String inputName = (String) request.getAttribute("inputName");

if ((Boolean) request.getAttribute("isFormPost")) {
    fieldValue = new ReferentialText(wp.param(String.class, inputName), Boolean.TRUE.equals(request.getAttribute("finalDraft")));
    state.putValue(fieldName, fieldValue);
    return;
}

// --- Presentation ---

wp.writeStart("div", "class", "inputSmall inputSmall-text");
wp.writeStart("textarea",
        "class", "richtext",
        "data-expandable-class", "code",
        "id", wp.getId(),
        "name", inputName,
        "data-user", wp.getObjectLabel(wp.getUser()),
        "data-user-id", wp.getUser() != null ? wp.getUser().getId() : null,
        "data-first-draft", Boolean.TRUE.equals(request.getAttribute("firstDraft")),
        "data-track-changes", !Boolean.TRUE.equals(request.getAttribute("finalDraft")));

if (fieldValue != null) {
    for (Object item : fieldValue) {

        if (item instanceof Reference) {
            Reference reference = (Reference) item;
            Object referenceObject = reference.getObject();
            if (referenceObject != null) {

                wp.write("<span class=\"enhancement");
                if (referenceObject instanceof ReferentialTextMarker) {
                    wp.write(" marker");
                }
                wp.write("\"");

                for (Map.Entry<String, Object> e : reference.entrySet()) {
                    if (!"record".equals(e.getKey())) {
                        wp.write(" data-");
                        wp.write(wp.h(e.getKey()));
                        wp.write("=\"");
                        wp.write(wp.h(e.getValue()));
                        wp.write("\"");
                    }
                }

                State referenceState = State.getInstance(referenceObject);
                wp.write(" data-id=\"");
                wp.write(referenceState.getId());
                wp.write("\"");

                ObjectType referenceType = referenceState.getType();
                if (referenceType != null) {
                    for (ObjectField referenceField : referenceType.getFields()) {
                        if (ObjectField.FILE_TYPE.equals(referenceField.getInternalType())) {
                            StorageItem file = (StorageItem) referenceState.getValue(referenceField.getInternalName());
                            if (file != null) {
                                wp.write(" data-preview=\"");
                                wp.write(wp.h(file.getUrl()));
                                wp.write("\"");
                            }
                        }
                    }
                }

                wp.write(">");
                wp.write(referenceState.getLabel());
                wp.write("</span>");
            }

        } else {
            wp.write(wp.h(item));
        }
    }
}

wp.writeEnd();
wp.writeEnd();
%><%!

private static void addStringToReferentialText(ReferentialText referentialText, String string) {
    string = StringUtils.replaceAll(string, "(?i)<p[^>]*>(?:\\s|&nbsp;)*</p>", "");
    string = string.trim();
    if (!ObjectUtils.isBlank(string)) {
        referentialText.add(string);
    }
}
%>
