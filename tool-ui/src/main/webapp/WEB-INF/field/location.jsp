<%@ page import="

com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.Location,
com.psddev.dari.db.ObjectField,
com.psddev.dari.db.State,
com.psddev.dari.util.HtmlWriter
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);
State state = State.getInstance(request.getAttribute("object"));
ObjectField field = (ObjectField) request.getAttribute("field");
String fieldName = field.getInternalName();
Object fieldValue = state.getValue(fieldName);
String inputName = ((String) request.getAttribute("inputName")) + "/";
String xInputName = inputName + "x";
String yInputName = inputName + "y";

if ((Boolean) request.getAttribute("isFormPost")) {
    Double x = wp.doubleParam(xInputName, null);
    Double y = wp.doubleParam(yInputName, null);
    fieldValue = x != null && y != null ? new Location(x, y) : null;
    state.putValue(fieldName, fieldValue);
    return;
}

// --- Presentation ---

HtmlWriter writer = new HtmlWriter(wp.getWriter());
Location location = (Location) fieldValue;

writer.start("div", "class", "smallInput");
    writer.string("Latitude").tag("br");
    writer.tag("input", "type", "text", "name", xInputName, "value", location != null ? location.getX() : null);
    writer.string("Longitude").tag("br");
    writer.tag("input", "type", "text", "name", yInputName, "value", location != null ? location.getY() : null);
writer.end();
%>
