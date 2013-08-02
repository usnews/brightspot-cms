<%@ page import="

com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.Region,
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

Region region = (Region) state.getValue(fieldName);
if (region == null) {
    region = Region.empty();
}

String inputName = ((String) request.getAttribute("inputName")) + "/";
String geoJsonName = inputName + "geoJson";
String zoomLevelName = inputName + "zoom";
String zoomStateName = fieldName + ".zoom";

if ((Boolean) request.getAttribute("isFormPost")) {
    region = Region.fromGeoJson(wp.param(geoJsonName));
    state.putValue(fieldName, region);

    Integer zoomLevel = wp.intParam(zoomLevelName, null);
    state.put(zoomStateName, zoomLevel);

    return;
}

Integer zoomLevel = ObjectUtils.to(Integer.class, state.get(zoomStateName));

pageContext.setAttribute("region", fieldValue);
pageContext.setAttribute("zoomLevelName", zoomLevelName);
pageContext.setAttribute("geoJsonName", geoJsonName);
pageContext.setAttribute("zoomLevel", zoomLevel == null ? 16 : zoomLevel);

// --- Presentation ---
%>
<script src="http://leafletjs.com/examples/sample-geojson.js"></script>
<script type="text/javascript">
    L.Icon.Default.imagePath = "<%= wp.cmsUrl("/style/leaflet") %>";
</script>

<div class='locationMap'>
    <input class="locationMapZoom" type="hidden" name="${zoomLevelName}" value="${zoomLevel}"/>
    <input class="locationMapGeoJson" type="hidden" name="${geoJsonName}" value="<%= wp.h(region.getGeoJson()) %>"/>
</div>
