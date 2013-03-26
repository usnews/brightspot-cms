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

State state = State.getInstance(request.getAttribute("object"));

ObjectField field = (ObjectField) request.getAttribute("field");
String fieldName = field.getInternalName();
ReferentialText fieldValue = (ReferentialText) state.getValue(fieldName);

String inputName = (String) request.getAttribute("inputName");

if ((Boolean) request.getAttribute("isFormPost")) {

    if (fieldValue == null) {
        fieldValue = new ReferentialText();
    } else {
        fieldValue.clear();
    }

    String newText = wp.param(inputName);
    if (newText != null) {

        Matcher enhancementMatcher = StringUtils.getMatcher(newText, "(?is)<([^>\\s]+)([^>]+class=(['\"])[^'\"]*enhancement[^'\"]*\\3[^>]*)>.*?</\\1>");
        int lastMatchAt = 0;
        while (enhancementMatcher.find()) {

            addStringToReferentialText(fieldValue, newText.substring(lastMatchAt, enhancementMatcher.start()));

            Reference reference = new Reference();
            String attrs = enhancementMatcher.group(2);
            Matcher attrMatcher = StringUtils.getMatcher(attrs, "data-([^=]+)=(['\"])((?:(?!\\2).)*)\\2");
            while (attrMatcher.find()) {
                String key = attrMatcher.group(1);
                if (!key.startsWith("_")) {
                    reference.put(key, attrMatcher.group(3));
                }
            }

            UUID referenceId = ObjectUtils.asUuid(reference.remove("id"));
            if (referenceId != null) {
                reference.put("record", Query.findById(Object.class, referenceId));
                fieldValue.add(reference);
            }

            lastMatchAt = enhancementMatcher.end();
        }

        addStringToReferentialText(fieldValue, newText.substring(lastMatchAt, newText.length()));
    }

    state.putValue(fieldName, fieldValue);
    return;
}

// --- Presentation ---

wp.write("<div class=\"smallInput\">");
wp.write("<textarea class=\"richtext\" id=\"", wp.getId(), "\" name=\"", wp.h(inputName), "\">");

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

wp.write("</textarea>");
wp.write("</div>");
%><%!

private static void addStringToReferentialText(ReferentialText referentialText, String string) {
    string = StringUtils.replaceAll(string, "(?i)<p[^>]*>(?:\\s|&nbsp;)*</p>", "");
    string = string.trim();
    if (!ObjectUtils.isBlank(string)) {
        referentialText.add(string);
    }
}
%>
