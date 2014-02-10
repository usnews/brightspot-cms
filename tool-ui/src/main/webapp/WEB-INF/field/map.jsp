<%@ page session="false" import="

com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.ObjectField,
com.psddev.dari.db.State,

com.psddev.dari.util.ObjectUtils
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);

State state = State.getInstance(request.getAttribute("object"));

ObjectField field = (ObjectField) request.getAttribute("field");
String fieldName = field.getInternalName();
Object fieldValue = state.getValue(fieldName);

String inputName = (String) request.getAttribute("inputName");

if ((Boolean) request.getAttribute("isFormPost")) {
    state.putValue(fieldName, ObjectUtils.fromJson(wp.param(inputName)));
    return;
}

// --- Presentation ---

%><div class="inputSmall">
    <textarea class="json" id="<%= wp.getId() %>" name="<%= wp.h(inputName) %>"><%= wp.h(ObjectUtils.toJson(fieldValue, true)) %></textarea>
</div>
