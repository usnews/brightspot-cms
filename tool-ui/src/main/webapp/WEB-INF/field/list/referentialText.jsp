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

java.util.ArrayList,
java.util.List,
java.util.Map,
java.util.UUID,
java.util.regex.Matcher
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);

State state = State.getInstance(request.getAttribute("object"));

ObjectField field = (ObjectField) request.getAttribute("field");
String fieldName = field.getInternalName();
List<ReferentialText> fieldValue = (List<ReferentialText>) state.getValue(fieldName);

String inputName = (String) request.getAttribute("inputName");

if ((Boolean) request.getAttribute("isFormPost")) {
    boolean finalDraft = state.isNew() || state.isVisible();
    fieldValue = new ArrayList<ReferentialText>();

    for (String newHtml : wp.params(String.class, inputName)) {
        fieldValue.add(new ReferentialText(newHtml, finalDraft));
    }

    state.putValue(fieldName, fieldValue);
    return;
}

// --- Presentation ---

wp.write("<div class=\"inputSmall repeatableInputs\"><ol>");

if (!ObjectUtils.isBlank(fieldValue)) {
    for (ReferentialText referentialText : fieldValue) {
        if (referentialText != null) {

            wp.write("<li><textarea class=\"richtext\" data-expandable-class=\"code\" id=\"", wp.createId(), "\" name=\"", wp.h(inputName), "\">");
            for (Object item : referentialText) {

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

                        wp.write(">");
                        wp.write(referenceState.getLabel());
                        wp.write("</span>");
                    }

                } else {
                    wp.write(wp.h(item));
                }
            }

            wp.write("</textarea></li>");
        }
    }
}

wp.write("<li class=\"template\"><textarea class=\"richtext\" data-expandable-class=\"code\" id=\"", wp.getId(), "\" name=\"", wp.h(inputName), "\"></textarea></li>");

wp.write("</ol></div>");
%><%!

private static void addStringToReferentialText(ReferentialText referentialText, String string) {
    string = StringUtils.replaceAll(string, "(?i)<p[^>]*>(?:\\s|&nbsp;)*</p>", "");
    string = string.trim();
    if (!ObjectUtils.isBlank(string)) {
        referentialText.add(string);
    }
}
%>
