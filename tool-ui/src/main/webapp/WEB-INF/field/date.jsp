<%@ page import="

com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.ObjectField,
com.psddev.dari.db.State,

com.psddev.dari.util.DateUtils,
com.psddev.dari.util.ObjectUtils,

java.util.Date
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);

State state = State.getInstance(request.getAttribute("object"));

ObjectField field = (ObjectField) request.getAttribute("field");
String fieldName = field.getInternalName();
Date fieldValue = (Date) state.getValue(fieldName);

String inputName = (String) request.getAttribute("inputName");

if ((Boolean) request.getAttribute("isFormPost")) {
    state.putValue(fieldName, wp.dateParam(inputName));
    return;
}

// --- Presentation ---

%><div class="smallInput">
    <input class="date" name="<%= wp.h(inputName) %>" type="text" value="<%= wp.h(DateUtils.toString(fieldValue, "yyyy-MM-dd HH:mm:ss")) %>">
</div>
