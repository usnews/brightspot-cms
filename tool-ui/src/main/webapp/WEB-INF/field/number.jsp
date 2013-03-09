<%@ page import="

com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.ObjectField,
com.psddev.dari.db.State
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);

State state = State.getInstance(request.getAttribute("object"));

ObjectField field = (ObjectField) request.getAttribute("field");
String fieldName = field.getInternalName();

String inputName = (String) request.getAttribute("inputName");

if ((Boolean) request.getAttribute("isFormPost")) {
    if (field.getStep() != null && field.getStep().doubleValue() * 10 % 10 == 0.0) {
        state.putValue(fieldName, wp.longParam(inputName));
    } else {
        state.putValue(fieldName, wp.doubleParam(inputName));
    }
    return;
}

// --- Presentation ---

%><div class="inputSmall">
    <input id="<%= wp.getId() %>" name="<%= wp.h(inputName) %>" type="text" value="<%= wp.h(state.getValue(fieldName)) %>">
</div>
