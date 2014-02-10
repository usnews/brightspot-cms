<%@ page session="false" import="

com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.ObjectField,
com.psddev.dari.db.State,

java.util.UUID
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);

State state = State.getInstance(request.getAttribute("object"));

ObjectField field = (ObjectField) request.getAttribute("field");
String fieldName = field.getInternalName();
UUID fieldValue = (UUID) state.getValue(fieldName);

String inputName = (String) request.getAttribute("inputName");

if ((Boolean) request.getAttribute("isFormPost")) {
    state.putValue(fieldName, wp.uuidParam(inputName));
    return;
}

// --- Presentation ---

%><div class="inputSmall">
    <input id="<%= wp.getId() %>" name="<%= wp.h(inputName) %>" type="text" value="<%= wp.h(fieldValue) %>">
</div>
