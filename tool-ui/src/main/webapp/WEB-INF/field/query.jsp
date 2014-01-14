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
com.psddev.dari.util.Settings,
com.psddev.dari.util.StorageItem,

java.util.ArrayList,
java.util.Collections,
java.util.Date,
java.util.List,
java.util.HashMap,
java.util.Map,
java.util.UUID
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);

State state = State.getInstance(request.getAttribute("object"));

ObjectField field = (ObjectField) request.getAttribute("field");

ObjectType internalType = null;
for (ObjectType t : field.getTypes()) {
    if (!Query.class.equals(t.getObjectClass())) {
        internalType = t;
        break;
    }
}
String fieldName = field.getInternalName();
Query<?> fieldValue = (Query<?>) state.getValue(fieldName);

boolean isEmbedded = field.isEmbedded();

String inputName = (String) request.getAttribute("inputName");

String predicate = fieldValue.toString();
if (fieldValue != null && fieldValue.getPredicate() != null) {
    predicate = fieldValue.getPredicate().toString();
}

fieldValue = Query.fromType(internalType);

if ((Boolean) request.getAttribute("isFormPost")) {

    predicate = wp.param(String.class, inputName);
    fieldValue.and(predicate);
    state.putByPath(fieldName, fieldValue);

    return;
}

// --- Presentation ---

%><div class="inputSmall">

<%

    wp.writeStart("div", "class", "searchFilter searchFilter-advancedQuery");
        wp.writeStart("textarea",
                "class", "code",
                "data-expandable-class", "code",
                "name", inputName,
                "placeholder", "Advanced Query");
            wp.writeHtml(predicate);
        wp.writeEnd();

        wp.writeStart("a",
                "class", "icon icon-action-edit icon-only",
                "href", wp.cmsUrl("/searchAdvancedQuery?typeId="+internalType.getId()),
                "target", "searchAdvancedQuery");
            wp.writeHtml("Edit");
        wp.writeEnd();
    wp.writeEnd();

%>

</div>
