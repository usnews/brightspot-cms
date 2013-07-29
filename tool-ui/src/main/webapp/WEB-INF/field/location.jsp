<%@ page import="

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

<style>
    .location-map { 
        left: 7px;
        width: 700px;
        height: 500px;
    }

    div[data-field=location] {
        min-height: 500px;
    }

    .leaflet-control-geosearch, .leaflet-control-geosearch ul {
        padding: 0px;
        margin: 10px auto;
        width: 50%;
        border-radius: 3px;
        border: none;
        background: none;
    }

    #leaflet-control-geosearch-qry {
        width: 100%;
        border-radius: 3px;
        display: block;
        box-shadow: 0 1px 7px rgba(0, 0, 0, 0.65);
        border: none;
        padding: 5px;
    }

    .leaflet-center {
        width: 100%;
    }

    .leaflet-top, .leaflet-bottom {
        z-index: 0;
    }

    .leaflet-control-geosearch {
        margin: 0 50px;
        z-index: -1;
    }
</style>
<script type="text/javascript">
    L.Icon.Default.imagePath = "<%= wp.cmsUrl("/style/leaflet") %>";
</script>

<div class='location-map'>
    <input class="location-map-lat" type="hidden" name="${xInputName}" value="${location.x}"/>
    <input class="location-map-long" type="hidden" name="${yInputName}" value="${location.y}"/>
    <input class="location-map-zoom" type="hidden" name="${zoomLevelName}" value="${zoomLevel}"/>
</div>

