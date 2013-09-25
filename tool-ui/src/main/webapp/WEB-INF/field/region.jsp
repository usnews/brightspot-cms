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

Region region = (Region) state.getValue(fieldName);
if (region == null) {
    region = Region.empty();
}

String inputName = ((String) request.getAttribute("inputName")) + "/";
String geoJsonName = inputName + "geoJson";
String zoomName = inputName + "zoom";
String zoomStateName = fieldName + ".leaflet.zoom";
String centerName = inputName + "bounds";
String centerStateName = fieldName + ".leaflet.center";

if ((Boolean) request.getAttribute("isFormPost")) {
    region = Region.fromGeoJson(wp.param(geoJsonName));

    state.putValue(fieldName, region);
    state.put(centerStateName, wp.param(centerName));
    state.put(zoomStateName, wp.intParam(zoomName));

    return;
}

String center = (String) state.get(centerStateName);
if (center == null) {
    center = "{\"lat\":39.8282, \"lng\":-98.5795}";
}

Integer zoom = ObjectUtils.to(Integer.class, state.get(zoomStateName));
if (zoom == null) {
    zoom = 4;
}

pageContext.setAttribute("zoomName", zoomName);
pageContext.setAttribute("centerName", centerName);
pageContext.setAttribute("geoJsonName", geoJsonName);

// --- Presentation ---
%>
<script type="text/javascript">
    L.Icon.Default.imagePath = "<%= wp.cmsUrl("/style/leaflet") %>";
</script>

<div class="inputSmall">
    <div class='regionMap'>
        <input class="regionMapZoom" type="hidden" name="${zoomName}" value="<%= wp.h(zoom) %>" />
        <input class="regionMapCenter" type="hidden" name="${centerName}" value="<%= wp.h(center) %>" />
        <input class="regionMapGeoJson" type="hidden" name="${geoJsonName}" value="<%= wp.h(region.getGeoJson()) %>"/>
    </div>
</div>
