<%@ page import="

com.psddev.cms.db.Content,
com.psddev.cms.db.ToolUi,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.ObjectField,
com.psddev.dari.db.ObjectFieldComparator,
com.psddev.dari.db.State,
com.psddev.dari.db.ObjectType,
com.psddev.dari.db.Query,
com.psddev.dari.util.ObjectUtils,
com.psddev.dari.util.PaginatedResult,
com.psddev.dari.util.StorageItem,

java.util.ArrayList,
java.util.Collections,
java.util.Date,
java.util.List,
java.util.HashMap,
java.util.Map,
java.util.Set,
java.util.UUID
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);

State state = State.getInstance(request.getAttribute("object"));

ObjectField field = (ObjectField) request.getAttribute("field");
String fieldName = field.getInternalName();
Object fieldValue = state.getValue(fieldName);

ObjectType fieldValueType = null;
Set<ObjectType> validTypes = field.findConcreteTypes();
if (validTypes.size() == 1) {
    for (ObjectType type : validTypes) {
        fieldValueType = type;
        break;
    }
}

boolean isEmbedded = field.isEmbedded() || (fieldValueType != null && fieldValueType.isEmbedded());
if (!isEmbedded) {
    isEmbedded = true;
    for (ObjectType type : validTypes) {
        if (!type.isEmbedded()) {
            isEmbedded = false;
            break;
        }
    }
}

String inputName = (String) request.getAttribute("inputName");
String idName = inputName + ".id";
String typeIdName = inputName + ".typeId";
String publishDateName = inputName + ".publishDate";

UUID id = wp.uuidParam(idName);
UUID typeId = wp.uuidParam(typeIdName);
Date publishDate = wp.dateParam(publishDateName);

if ((Boolean) request.getAttribute("isFormPost") && fieldValue != null && !State.getInstance(fieldValue).getTypeId().equals(typeId)) {
    fieldValue = null;
}

if (fieldValue == null && isEmbedded) {
    if (fieldValueType != null) {
        fieldValue = fieldValueType.createObject(null);
    } else {
        for (ObjectType type : validTypes) {
            if (type.getId().equals(typeId)) {
                fieldValue = type.createObject(null);
                break;
            }
        }
    }
}

if ((Boolean) request.getAttribute("isFormPost")) {
    if (isEmbedded) {
        if (fieldValue != null) {
            State fieldValueState = State.getInstance(fieldValue);
            fieldValueState.setId(id);
            wp.include("/WEB-INF/objectPost.jsp", "object", fieldValue);
            fieldValueState.putValue(Content.PUBLISH_DATE_FIELD, publishDate != null ? publishDate : new Date());
            fieldValueState.putValue(Content.UPDATE_DATE_FIELD, new Date());
        }
    } else {
        fieldValue = Query.findById(Object.class, wp.uuidParam(inputName));
    }
    state.putValue(fieldName, fieldValue);
    return;
}

// --- Presentation ---

if (isEmbedded) {
    if (fieldValueType != null) {
        State fieldValueState = State.getInstance(fieldValue);
        Date fieldValuePublishDate = fieldValueState.as(Content.ObjectModification.class).getPublishDate();
        wp.write("<div class=\"inputLarge\">");
        wp.write("<input name=\"", wp.h(idName), "\" type=\"hidden\" value=\"", fieldValueState.getId(), "\">");
        wp.write("<input name=\"", wp.h(typeIdName), "\" type=\"hidden\" value=\"", fieldValueState.getTypeId(), "\">");
        wp.write("<input name=\"", wp.h(publishDateName), "\" type=\"hidden\" value=\"", wp.h(fieldValuePublishDate != null ? fieldValuePublishDate.getTime() : null), "\">");
        wp.include("/WEB-INF/objectForm.jsp", "object", fieldValue);
        wp.write("</div>");

    } else {
        List<Object> validObjects = new ArrayList<Object>();
        for (ObjectType type : validTypes) {
            if (fieldValue != null && type.equals(State.getInstance(fieldValue).getType())) {
                validObjects.add(fieldValue);
            } else {
                validObjects.add(type.createObject(null));
            }
        }

        Collections.sort(validObjects, new ObjectFieldComparator("_type/_label", false));

        String validObjectClass = wp.createId();
        Map<UUID, String> showClasses = new HashMap<UUID, String>();
        wp.write("<div class=\"inputSmall\">");
        wp.write("<select class=\"toggleable\" name=\"", wp.h(idName), "\">");
        wp.write("<option data-hide=\".", validObjectClass, "\" value=\"\"></option>");
        for (Object validObject : validObjects) {
            State validState = State.getInstance(validObject);
            String showClass = wp.createId();
            showClasses.put(validState.getId(), showClass);
            wp.write("<option data-hide=\".", validObjectClass, "\" data-show=\".", showClass, "\" value=\"", validState.getId(), "\"");
            if (validObject.equals(fieldValue)) {
                wp.write(" selected");
            }
            wp.write(">");
            wp.write(wp.objectLabel(validState.getType()));
            wp.write("</option>");
        }
        wp.write("</select>");
        wp.write("</div>");

        for (Object validObject : validObjects) {
            State validState = State.getInstance(validObject);
            Date validObjectPublishDate = validState.as(Content.ObjectModification.class).getPublishDate();
            wp.write("<div class=\"inputLarge ", validObjectClass, " ", showClasses.get(validState.getId()), "\">");
            wp.write("<input name=\"", wp.h(typeIdName), "\" type=\"hidden\" value=\"", validState.getTypeId(), "\">");
            wp.write("<input name=\"", wp.h(publishDateName), "\" type=\"hidden\" value=\"", wp.h(validObjectPublishDate != null ? validObjectPublishDate.getTime() : null), "\">");
            wp.include("/WEB-INF/objectForm.jsp", "object", validObject);
            wp.write("</div>");
        }
    }

    return;
}

%><div class="inputSmall">
    <% wp.writeObjectSelect(field, fieldValue, "name", inputName); %>
</div>
