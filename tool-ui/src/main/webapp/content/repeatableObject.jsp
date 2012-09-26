<%@ page import="

com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.ObjectType,
com.psddev.dari.db.State
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);
String inputName = wp.param("inputName");
ObjectType type = ObjectType.getInstance(wp.uuidParam("typeId"));
Object object = type.createObject(null);
State objectState = State.getInstance(object);

// --- Presentation ---

%><input type="hidden" name="<%= wp.h(inputName) %>.id" value="<%= objectState.getId() %>" />
<input type="hidden" name="<%= wp.h(inputName) %>.typeId" value="<%= type.getId() %>" />
<input type="hidden" name="<%= wp.h(inputName) %>.publishDate" value="" />
<% wp.include("/WEB-INF/objectForm.jsp", "object", object); %>
