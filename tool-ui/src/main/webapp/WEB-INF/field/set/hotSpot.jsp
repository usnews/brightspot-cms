<%@page import="com.psddev.image.HotSpot"%>
<%@page import="com.psddev.image.HotSpots"%>
<%@ page session="false" import="
         
com.psddev.cms.db.Content,
com.psddev.cms.db.ToolUi,
com.psddev.cms.tool.PageWriter,
com.psddev.cms.tool.Search,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.Query,
com.psddev.dari.db.ObjectField,
com.psddev.dari.db.ObjectFieldComparator,
com.psddev.dari.db.ObjectType,
com.psddev.dari.db.State,

com.psddev.dari.util.ObjectUtils,
com.psddev.dari.util.StorageItem,

java.util.ArrayList,
java.util.Collections,
java.util.Date,
java.util.HashMap,
java.util.LinkedHashSet,
java.util.List,
java.util.Map,
java.util.Set,
java.util.UUID
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);

State state = State.getInstance(request.getAttribute("object"));
if (state.getOriginalObject() instanceof HotSpots) {
    ObjectField field = (ObjectField) request.getAttribute("field");
    String fieldName = field.getInternalName();
    StorageItem fieldValue = (StorageItem) state.getByPath(fieldName);
    HotSpots hotspots = ObjectUtils.to(HotSpots.class, state.getOriginalObject());
    if (fieldValue != null &&
            hotspots.getHotSpotImage() != null &&
            hotspots.getHotSpotImage().equals(fieldValue)) {

        List<HotSpot> hotspotList = state.as(HotSpots.Data.class).getHotSpots();

        String inputName = (String) request.getAttribute("inputName");
        String idName = inputName + ".id";
        String typeIdName = inputName + ".typeId";
        String publishDateName = inputName + ".publishDate";

        List<ObjectType> validTypes = new ArrayList<ObjectType>();
        validTypes.addAll(ObjectType.getInstance(HotSpot.class).findConcreteTypes());

        Collections.sort(validTypes, new ObjectFieldComparator("_label", false));

        if ((Boolean) request.getAttribute("isFormPost")) {
            //Todo
            //state.putValue(fieldName, fieldValue);
            return;

        }
        // --- Presentation ---

        %>
        <div class="inputSmall">
            <div class="inputLarge repeatableForm hotSpots">
                <ul>
                    <%
                    for (HotSpot item : hotspotList) {
                        State itemState = State.getInstance(item);
                        ObjectType itemType = itemState.getType();
                        Date itemPublishDate = itemState.as(Content.ObjectModification.class).getPublishDate();
                        %>
                        <li data-type="<%= wp.objectLabel(itemType) %>" data-label="<%= wp.objectLabel(item) %>">
                            <input name="<%= wp.h(idName) %>" type="hidden" value="<%= itemState.getId() %>">
                            <input name="<%= wp.h(typeIdName) %>" type="hidden" value="<%= itemType.getId() %>">
                            <input name="<%= wp.h(publishDateName) %>" type="hidden" value="<%= wp.h(itemPublishDate != null ? itemPublishDate.getTime() : null) %>">
                            <% wp.writeFormFields(item); %>
                        </li>
                    <% } %>
                    <% for (ObjectType type : validTypes) { %>
                        <script type="text/template">
                            <li data-type="<%= wp.objectLabel(type) %>">
                                <a href="<%= wp.cmsUrl("/content/repeatableObject.jsp", "inputName", inputName, "typeId", type.getId()) %>"></a>
                            </li>
                        </script>
                    <% } %>
                </ul>
            </div>
        </div>
    <% }
}%>

