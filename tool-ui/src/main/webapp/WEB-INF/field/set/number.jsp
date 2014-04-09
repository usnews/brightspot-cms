<%@ page session="false" import="

com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.ObjectField,
com.psddev.dari.db.State,

com.psddev.dari.util.ObjectUtils,

java.util.Collection,
java.util.Set,
java.util.TreeSet
" %><%

ToolPageContext wp = new ToolPageContext(pageContext);
State state = State.getInstance(request.getAttribute("object"));
ObjectField field = (ObjectField) request.getAttribute("field");
String fieldName = field.getInternalName();
Set<Object> fieldValue;
Object fieldValueObject = state.getValue(fieldName);

if (fieldValueObject instanceof Set) {
    fieldValue = (Set<Object>) fieldValueObject;

} else if (fieldValueObject instanceof Collection) {
    fieldValue = new TreeSet<Object>((Collection<Object>) fieldValueObject);

} else {
    fieldValue = null;
}

String inputName = (String) request.getAttribute("inputName");
String textName = inputName + ".text";
String toggleName = inputName + ".toggle";

if ((Boolean) request.getAttribute("isFormPost")) {
    fieldValue = new TreeSet<Object>();
    String[] texts = wp.params(textName);
    String[] toggles = wp.params(toggleName);

    for (int i = 0, s = Math.min(texts.length, toggles.length); i < s; i ++) {
        String text = texts[i];

        if (Boolean.parseBoolean(toggles[i]) && !ObjectUtils.isBlank(text)) {
            fieldValue.add(text);
        }
    }

    state.putValue(fieldName, fieldValue);
    return;
}

wp.writeStart("div", "class", "inputSmall repeatableText");
    wp.writeStart("ul");
        if (fieldValue != null) {
            for (Object text : fieldValue) {
                wp.writeStart("li");
                    wp.writeElement("input",
                            "type", "checkbox",
                            "name", toggleName,
                            "value", true,
                            "checked", "checked");

                    wp.writeElement("input",
                            "type", "text",
                            "class", "expandable",
                            "name", textName,
                            "value", text);
                wp.writeEnd();
            }
        }

        wp.writeStart("li", "class", "template");
            wp.writeElement("input",
                    "type", "checkbox",
                    "name", toggleName,
                    "value", true);

            wp.writeElement("input",
                    "type", "text",
                    "class", "expandable",
                    "name", textName);
        wp.writeEnd();
    wp.writeEnd();
wp.writeEnd();
%>
