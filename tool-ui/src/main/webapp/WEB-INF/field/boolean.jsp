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
    state.putValue(fieldName, Boolean.parseBoolean(wp.param(inputName)));
    return;
}

// --- Presentation ---

%><div class="smallInput">
    <input<%= Boolean.TRUE.equals(state.getValue(fieldName)) ? " checked" : "" %> id="<%= wp.getId() %>" name="<%= wp.h(inputName) %>" type="checkbox" value="true">
    <input name="<%= wp.h(inputName) %>" type="hidden" value="false">
</div>
