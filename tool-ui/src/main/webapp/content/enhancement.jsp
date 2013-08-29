<%@ page import="

com.psddev.cms.db.RichTextReference,
com.psddev.cms.db.ToolUi,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.Database,
com.psddev.dari.db.ObjectType,
com.psddev.dari.db.Query,
com.psddev.dari.db.Reference,
com.psddev.dari.db.State,
com.psddev.dari.util.ObjectUtils,
com.psddev.dari.util.StringUtils,

java.util.Arrays,
java.util.HashSet,
java.util.Map,
java.util.Set,
java.util.UUID
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);
if (wp.requireUser()) {
    return;
}

String pageId = wp.createId();

// init enhancement object
Object object = wp.findOrReserve();
State state = State.getInstance(object);

UUID typeId = wp.param(UUID.class, "typeId");
if (typeId == null && (state == null || state.isNew())) {
    object = null;
}

//init reference
Reference ref = null;

Map<?, ?> refMap = (Map<?, ?>) ObjectUtils.fromJson(wp.paramOrDefault(String.class, "reference", "{}"));
UUID refId = ObjectUtils.to(UUID.class, ((Map<?, ?>) refMap).remove("_id"));
UUID refTypeId = ObjectUtils.to(UUID.class, ((Map<?, ?>) refMap).remove("_type"));

ObjectType refType = null;
if (state != null) {
    Class<? extends Reference> referenceClass = state.getType().as(ToolUi.class).getReferenceableViaClass();
    if (referenceClass != null) {
        refType = Database.Static.getDefault().getEnvironment().getTypeByClass(referenceClass);
    }
}

if (refType == null) {
    refType = Database.Static.getDefault().getEnvironment().getTypeById(refTypeId);
}

if (refType != null) {
    Object refObject = refType.createObject(refId);
    if (refObject instanceof Reference) {
        ref = (Reference) refObject;
    }
}

if (ref == null) {
    ref = new Reference();
}

for (Map.Entry<?, ?> entry : ((Map<?, ?>) refMap).entrySet()) {
    Object key = entry.getKey();
    ref.getState().put(key != null ? key.toString() : null, entry.getValue());
}

ref.setObject(object);

// Always reset the label and preview to the current object
RichTextReference rteRef = ref.as(RichTextReference.class);
rteRef.setLabel(state != null ? state.getLabel() : null);
rteRef.setPreview(state != null && state.getPreview() != null ? state.getPreview().getPublicUrl() : null);

boolean isEditRef = wp.param(boolean.class, "isEditRef");
if (isEditRef) {
    request.setAttribute("excludeFields", Arrays.asList("record"));
}

if (object != null && wp.isFormPost()) {
    try {
        wp.updateUsingParameters(isEditRef ? ref : object);
        if (!isEditRef) {
            wp.publish(object);
        }
    } catch (Exception ex) {
        wp.getErrors().add(ex);
    }
}

// --- Presentation ---

if (object == null) {
    Set<UUID> validTypeIds = new HashSet<UUID>();
    for (ObjectType type : Database.Static.getDefault().readAll(Query.from(ObjectType.class))) {
        if (type.as(ToolUi.class).isReferenceable()) {
            validTypeIds.add(type.getId());
        }
    }
    wp.include("/WEB-INF/search.jsp"
            , "newJsp", StringUtils.addQueryParameters("/content/enhancement.jsp", "reference", referenceParamWithoutObject(ref))
            , "resultJsp", StringUtils.addQueryParameters("/content/enhancementResult.jsp", "reference", referenceParamWithoutObject(ref))
            , "validTypeIds", validTypeIds.toArray(new UUID[validTypeIds.size()])
            );

} else {
    wp.writeFormHeading(object);
    %>

    <form action="<%= wp.url("", "typeId", state.getTypeId(), "id", state.getId()) %>" enctype="multipart/form-data" id="<%= pageId %>" method="post">
        <ul class="breadcrumb">
            <li>
                <a class="action action-change" href="<%= wp.url("",
                        "typeId", null,
                        "id", null,
                        "reference", referenceParamWithoutObject(ref),
                        "isEditRef", null) %>">Change Enhancement</a>
            </li>
            <li>
                <a class="action action-edit" href="<%= wp.url("",
                        "reference", referenceParamWithoutObject(ref),
                        "isEditRef", !isEditRef) %>"><%= isEditRef ? "Edit Enhancement" : "Edit Reference Metadata" %></a>
            </li>
        </ul>

        <% wp.include("/WEB-INF/errors.jsp"); %>
        <% wp.writeFormFields(isEditRef ? ref : object); %>
        <div class="buttons">
            <button class="action action-save">Save</button>
        </div>
    </form>

    <script type="text/javascript">
        if (typeof jQuery !== 'undefined') (function($) {
            var $source = $('#<%= pageId %>').popup('source');
            var href = $source.attr('href');
            href = href.replace(/([?&])id=[^&]*/, '$1');
            href = href.replace(/([?&])reference=[^&]*/, '$1');
            href += '&id=<%= state.getId() %>';
            href += '&reference=<%= wp.js(StringUtils.encodeUri(ObjectUtils.toJson(ref.getState().getSimpleValues()))) %>';
            $source.attr('href', href);
            $source.rte('enhancement', {
                'reference': '<%= wp.js(ObjectUtils.toJson(ref.getState().getSimpleValues())) %>',
                'id': '<%= state.getId() %>',
                <%-- backward compatibility + rte css attribute selector support --%>
                'label': '<%= wp.js(state.getLabel()) %>',
                'preview': '<%= wp.js(state.getPreview() != null ? state.getPreview().getPublicUrl() : null) %>'
            });
        })(jQuery);
    </script>
<% } %>
<%!

private static String referenceParamWithoutObject(Reference reference) {
    Map<String, Object> map = reference.getState().getSimpleValues();
    map.remove("record");
    return ObjectUtils.toJson(map);
}

%>