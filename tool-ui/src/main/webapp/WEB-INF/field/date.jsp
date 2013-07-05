<%@ page import="

com.psddev.cms.db.ToolUi,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.ObjectField,
com.psddev.dari.db.State,

java.util.Date,

org.joda.time.DateTime
" %><%

ToolPageContext wp = new ToolPageContext(pageContext);
State state = State.getInstance(request.getAttribute("object"));
ObjectField field = (ObjectField) request.getAttribute("field");
String inputName = (String) request.getAttribute("inputName");
String fieldName = field.getInternalName();
Date fieldValue = (Date) state.get(fieldName);

if (Boolean.TRUE.equals(request.getAttribute("isFormPost"))) {
    state.put(fieldName, wp.param(Date.class, inputName));
    return;
}

wp.writeStart("div", "class", "inputSmall");
    wp.writeTag("input",
            "type", "text",
            "class", "date",
            "name", inputName,
            "value", fieldValue != null ? new DateTime(fieldValue).toString("yyyy-MM-dd HH:mm:ss") : null,
            "placeholder", field.as(ToolUi.class).getPlaceholder());
wp.writeEnd();
%>
