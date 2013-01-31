<%@ page import="

com.psddev.cms.db.ToolUi,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.ObjectField,
com.psddev.dari.db.State,
com.psddev.dari.util.ObjectUtils,

java.util.Set
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);

State state = State.getInstance(request.getAttribute("object"));

ObjectField field = (ObjectField) request.getAttribute("field");
ToolUi ui = field.as(ToolUi.class);
String fieldName = field.getInternalName();
Object fieldValue = state.getValue(fieldName);

String inputName = (String) request.getAttribute("inputName");

String placeholder = ui.getPlaceholder();
if (field.isRequired()) {
    if (ObjectUtils.isBlank(placeholder)) {
        placeholder = "(Required)";
    } else {
        placeholder += " (Required)";
    }
}

Number suggestedMinimum = ui.getSuggestedMinimum();
Number suggestedMaximum = ui.getSuggestedMaximum();

if ((Boolean) request.getAttribute("isFormPost")) {
    state.putValue(fieldName, wp.param(inputName));
    return;
}

// --- Presentation ---

wp.write("<div class=\"smallInput\">");

Set<ObjectField.Value> validValues = field.getValues();
if (validValues != null) {
    wp.write("<select id=\"", wp.getId(), "\" name=\"", wp.h(inputName), "\">");
    wp.write("<option value=\"\">");
    wp.write(wp.h(placeholder));
    wp.write("</option>");
    for (ObjectField.Value value : validValues) {
        wp.write("<option");
        if (ObjectUtils.equals(value.getValue(), fieldValue)) {
            wp.write(" selected");
        }
        wp.write(" value=\"", wp.h(value.getValue()), "\">");
        wp.write(wp.h(value.getLabel()));
        wp.write("</option>");
    }
    wp.write("</select>");

} else {
    wp.write("<textarea id=\"", wp.getId());
    wp.write("\" placeholder=\"", wp.h(placeholder));
    if (suggestedMinimum != null) {
        wp.write("\" data-suggested-minimum=\"", suggestedMinimum.intValue());
    }
    if (suggestedMaximum != null) {
        wp.write("\" data-suggested-maximum=\"", suggestedMaximum.intValue());
    }
    wp.write("\" name=\"", wp.h(inputName));
    wp.write("\"");

    if (ui.isRichText()) {
        wp.write(" class=\"richtext\" data-use-line-breaks=\"true\"");
    }

    wp.write(">");
    wp.write(wp.h(fieldValue));
    wp.write("</textarea>");
}

wp.write("</div>");
%>
