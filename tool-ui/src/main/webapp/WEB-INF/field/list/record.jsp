<%@ page import="

com.psddev.cms.db.Content,
com.psddev.cms.db.ToolUi,
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
java.util.List,
java.util.Set,
java.util.UUID
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);

State state = State.getInstance(request.getAttribute("object"));

ObjectField field = (ObjectField) request.getAttribute("field");
String fieldName = field.getInternalName();
List<Object> fieldValue = (List<Object>) state.getValue(fieldName);
if (fieldValue == null) {
    fieldValue = new ArrayList<Object>();
}

List<ObjectType> validTypes = new ArrayList<ObjectType>(field.findConcreteTypes());
boolean isValueExternal = true;
if (validTypes != null && validTypes.size() > 0) {
    isValueExternal = false;
    for (ObjectType type : validTypes) {
        if (!type.isEmbedded()) {
            isValueExternal = true;
            break;
        }
    }
}

Collections.sort(validTypes, new ObjectFieldComparator("_label", false));

String inputName = (String) request.getAttribute("inputName");
String idName = inputName + ".id";
String typeIdName = inputName + ".typeId";
String publishDateName = inputName + ".publishDate";

if ((Boolean) request.getAttribute("isFormPost")) {

    fieldValue.clear();

    if (!isValueExternal) {
        UUID[] ids = wp.uuidParams(idName);
        UUID[] typeIds = wp.uuidParams(typeIdName);
        Date[] publishDates = wp.dateParams(publishDateName);
        for (int i = 0, s = Math.min(Math.min(ids.length, typeIds.length), publishDates.length); i < s; ++ i) {
            ObjectType type = ObjectType.getInstance(typeIds[i]);
            Object item = type.createObject(null);
            State itemState = State.getInstance(item);
            itemState.setId(ids[i]);
            wp.include("/WEB-INF/objectPost.jsp", "object", item);
            itemState.putValue(Content.PUBLISH_DATE_FIELD, publishDates[i] != null ? publishDates[i] : new Date());
            itemState.putValue(Content.UPDATE_DATE_FIELD, new Date());
            fieldValue.add(item);
        }

    } else {
        for (UUID id : wp.uuidParams(inputName)) {
            Object item = Query.findById(Object.class, id);
            if (item != null) {
                fieldValue.add(item);
            }
        }
    }

    state.putValue(fieldName, fieldValue);
    return;
}

// --- Presentation ---

%><% if (!isValueExternal) { %>
    <div class="largeInput repeatableForm">
        <ol>
            <%
            for (Object item : fieldValue) {
                State itemState = State.getInstance(item);
                ObjectType itemType = itemState.getType();
                Date itemPublishDate = itemState.as(Content.ObjectModification.class).getPublishDate();
                %>
                <li data-type="<%= wp.objectLabel(itemType) %>" data-label="<%= wp.objectLabel(item) %>">
                    <input name="<%= wp.h(idName) %>" type="hidden" value="<%= itemState.getId() %>">
                    <input name="<%= wp.h(typeIdName) %>" type="hidden" value="<%= itemType.getId() %>">
                    <input name="<%= wp.h(publishDateName) %>" type="hidden" value="<%= wp.h(itemPublishDate != null ? itemPublishDate.getTime() : null) %>">
                    <% wp.include("/WEB-INF/objectForm.jsp", "object", item); %>
                </li>
            <% } %>
            <% for (ObjectType type : validTypes) { %>
                <li class="template" data-type="<%= wp.objectLabel(type) %>">
                    <a href="<%= wp.cmsUrl("/content/repeatableObject.jsp", "inputName", inputName, "typeId", type.getId()) %>"></a>
                </li>
            <% } %>
        </ol>
    </div>

<%
} else {
    Set<ObjectType> valueTypes = field.getTypes();
    String validTypeIds;
    if (ObjectUtils.isBlank(valueTypes)) {
        validTypeIds = "";
    } else {
        StringBuilder sb = new StringBuilder();
        for (ObjectType type : valueTypes) {
            sb.append(type.getId()).append(",");
        }
        sb.setLength(sb.length() - 1);
        validTypeIds = sb.toString();
    }
    %>
    <div class="smallInput repeatableObjectId">
        <ol>
            <%
            if (fieldValue != null) {
                for (Object item : fieldValue) {
                    StorageItem preview = item != null ?
                            State.getInstance(item).getPreview() :
                            null;
                    %>
                    <li><input
                            class="objectId"
                            data-searcher-path="<%= wp.h(field.as(ToolUi.class).getInputSearcherPath()) %>"
                            data-label="<%= (validTypes == null || validTypes.size() != 1 ? wp.typeLabel(item) + ": " : "") + wp.objectLabel(item) %>"
                            data-typeIds="<%= wp.h(validTypeIds) %>"
                            data-pathed="<%= ToolUi.isOnlyPathed(field) %>"
                            data-additional-query="<%= wp.h(field.getPredicate()) %>"
                            data-preview="<%= preview != null ? preview.getUrl() : "" %>"
                            name="<%= wp.h(inputName) %>"
                            type="text"
                            value="<%= State.getInstance(item).getId() %>"
                            ></li>
                    <%
                }
            }
            %>
            <li class="template"><input
                    class="objectId"
                    data-searcher-path="<%= wp.h(field.as(ToolUi.class).getInputSearcherPath()) %>"
                    data-typeIds="<%= wp.h(validTypeIds) %>"
                    data-pathed="<%= ToolUi.isOnlyPathed(field) %>"
                    data-additional-query="<%= wp.h(field.getPredicate()) %>"
                    name="<%= wp.h(inputName) %>"
                    type="text"
                    ></li>
        </ol>
    </div>
<% } %>
