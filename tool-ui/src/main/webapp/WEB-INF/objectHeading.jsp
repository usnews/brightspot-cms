<%@ page import="

com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.State,
com.psddev.dari.db.ObjectType

" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);
Object object = request.getAttribute("object");
State state = State.getInstance(object);
ObjectType type = state.getType();

// --- Presentation ---

wp.write("<h1>");
if (state.isNew()) {
    wp.write("New ", wp.objectLabel(type));
} else {
    wp.write("Edit ", wp.objectLabel(type));
    wp.write(": <strong>", wp.objectLabel(object), "</strong>");
}
wp.write("</h1>");
%>
