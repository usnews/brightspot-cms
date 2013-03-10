<%@ page import="

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

ObjectField field = (ObjectField) request.getAttribute("field");
String fieldName = field.getInternalName();
Set<Object> fieldValue = (Set<Object>) state.getValue(fieldName);
if (fieldValue == null) {
    fieldValue = new LinkedHashSet<Object>();
}

List<ObjectType> validTypes = new ArrayList<ObjectType>(field.findConcreteTypes());
boolean isValueExternal = !field.isEmbedded();
if (isValueExternal && validTypes != null && validTypes.size() > 0) {
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
    if (!isValueExternal) {
        Map<UUID, Object> existing = new HashMap<UUID, Object>();

        for (Object item : fieldValue) {
            existing.put(State.getInstance(item).getId(), item);
        }

        fieldValue.clear();

        UUID[] ids = wp.uuidParams(idName);
        UUID[] typeIds = wp.uuidParams(typeIdName);
        Date[] publishDates = wp.dateParams(publishDateName);

        for (int i = 0, s = Math.min(Math.min(ids.length, typeIds.length), publishDates.length); i < s; ++ i) {
            Object item = existing.get(ids[i]);
            State itemState = State.getInstance(item);

            if (item != null) {
                itemState.setTypeId(typeIds[i]);

            } else {
                ObjectType type = ObjectType.getInstance(typeIds[i]);
                item = type.createObject(null);
                itemState = State.getInstance(item);
                itemState.setId(ids[i]);
            }

            wp.include("/WEB-INF/objectPost.jsp", "object", item);
            itemState.putValue(Content.PUBLISH_DATE_FIELD, publishDates[i] != null ? publishDates[i] : new Date());
            itemState.putValue(Content.UPDATE_DATE_FIELD, new Date());
            fieldValue.add(item);
        }

    } else {
        fieldValue.clear();

        for (UUID id : wp.uuidParams(inputName)) {
            Object item = Query.findById(Object.class, id);
            if (item != null) {
                fieldValue.add(item);
            }
        }
    }

    if (ToolUi.isFieldSorted(field)) {
        List<Object> sorted = new ArrayList<Object>(fieldValue);
        ObjectUtils.sort(sorted, false);
        fieldValue = new LinkedHashSet<Object>(sorted);
    }

    state.putValue(fieldName, fieldValue);
    return;

} else {
    if (ToolUi.isFieldSorted(field)) {
        List<Object> sorted = new ArrayList<Object>(fieldValue);
        ObjectUtils.sort(sorted, false);
        fieldValue = new LinkedHashSet<Object>(sorted);
    }
}

// --- Presentation ---

%><% if (!isValueExternal) { %>
    <div class="inputLarge repeatableForm">
        <ul>
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
        </ul>
    </div>

<%
} else {
    PageWriter writer = wp.getWriter();

    if (wp.isObjectSelectDropDown(field)) {
        writer.start("div", "class", "inputSmall");
            List<?> items = new Search(field).toQuery().selectAll();
            Collections.sort(items, new ObjectFieldComparator("_label", false));

            writer.start("select",
                    "multiple", "multiple",
                    "data-searchable", "true",
                    "name", inputName);
                for (Object item : items) {
                    State itemState = State.getInstance(item);
                    writer.start("option",
                            "selected", fieldValue.contains(item) ? "selected" : null,
                            "value", itemState.getId());
                        writer.objectLabel(item);
                    writer.end();
                }
            writer.end();
        writer.end();

    } else {
        writer.start("div", "class", "inputSmall repeatableObjectId");
            writer.start("ul");
                if (fieldValue != null) {
                    for (Object item : fieldValue) {
                        writer.start("li");
                            wp.writeObjectSelect(field, item, "name", inputName);
                        writer.end();
                    }
                }
                writer.start("li", "class", "template");
                    wp.writeObjectSelect(field, null, "name", inputName);
                writer.end();
            writer.end();
        writer.end();
    }
}
%>
