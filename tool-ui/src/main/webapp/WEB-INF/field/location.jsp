<%@ page session="false" import="

com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.Location,
com.psddev.dari.db.ObjectField,
com.psddev.dari.db.State,
com.psddev.dari.util.HtmlWriter,
com.psddev.dari.util.ObjectUtils
"%><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);
State state = State.getInstance(request.getAttribute("object"));
ObjectField field = (ObjectField) request.getAttribute("field");
String fieldName = field.getInternalName();
Object fieldValue = state.getValue(fieldName);

String zoomStateName = fieldName + ".zoom";
String inputName = ((String) request.getAttribute("inputName")) + "/";
String xInputName = inputName + "x";
String yInputName = inputName + "y";
String zoomLevelName = inputName + "zoom";

if ((Boolean) request.getAttribute("isFormPost")) {
    Double x = wp.doubleParam(xInputName, null);
    Double y = wp.doubleParam(yInputName, null);
    fieldValue = x != null && y != null ? new Location(x, y) : null;
    state.putValue(fieldName, fieldValue);

    Integer zoomLevel = wp.intParam(zoomLevelName, null);
    state.put(zoomStateName, zoomLevel);

    return;
}

Integer zoomLevel = ObjectUtils.to(Integer.class, state.get(zoomStateName));

pageContext.setAttribute("location", fieldValue);
pageContext.setAttribute("xInputName", xInputName);
pageContext.setAttribute("yInputName", yInputName);
pageContext.setAttribute("zoomLevelName", zoomLevelName);
pageContext.setAttribute("zoomLevel", zoomLevel == null ? 16 : zoomLevel);

// --- Presentation ---
%>

<div class="inputSmall">
    <div class='locationMap'>
        <input class="locationMapLatitude" type="hidden" name="${xInputName}" value="${location.x}"/>
        <input class="locationMapLongitude" type="hidden" name="${yInputName}" value="${location.y}"/>
        <input class="locationMapZoom" type="hidden" name="${zoomLevelName}" value="${zoomLevel}"/>
    </div>
</div>

