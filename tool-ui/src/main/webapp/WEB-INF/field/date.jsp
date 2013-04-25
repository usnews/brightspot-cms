<%@ page import="

java.util.Date,

com.psddev.cms.tool.ToolPageContext,
com.psddev.dari.db.ObjectField,
com.psddev.dari.db.State,

org.joda.time.DateTime
" %><%

ToolPageContext wp = new ToolPageContext(pageContext);
State state = State.getInstance(request.getAttribute("object"));
String fieldName = ((ObjectField) request.getAttribute("field")).getInternalName();
String inputName = (String) request.getAttribute("inputName");

state.putValue(fieldName, wp.param(Date.class, inputName));

wp.writeStart("div", "class", "inputSmall");
    wp.writeTag("input",
            "type", "text",
            "class", "date",
            "name", inputName,
            "value", new DateTime(state.get(fieldName)).toString("yyyy-MM-dd HH:mm:ss"));
wp.writeEnd();
%>
